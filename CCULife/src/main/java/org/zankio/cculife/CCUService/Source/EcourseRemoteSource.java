package org.zankio.cculife.CCUService.Source;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.Authentication.CookieAuth;
import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.CCUService.Helper.ConnectionHelper;
import org.zankio.cculife.CCUService.Kiki;
import org.zankio.cculife.CCUService.Parser.EcourseParser;
import org.zankio.cculife.CCUService.Parser.IParser;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;
import java.util.ArrayList;

public class EcourseRemoteSource extends EcourseSource {

    private Ecourse ecourse;
    private Ecourse.Course currentCourse;
    private ConnectionHelper connectionHelper;
    private CookieAuth auth;
    private EcourseParser parser;
    private EcourseLocalSource ecourseLocalSource;
    private SessionManager sessionManager;

    private static final String SESSION_FIELD_NAME = "PHPSESSID";
    private static final String ECOURSE_URL_LOGIN = "http://ecourse.elearning.ccu.edu.tw/php/index_login.php";
    private static final String ECOURSE_URL_COURSE_SELECT = "http://ecourse.elearning.ccu.edu.tw/php/login_s.php?courseid=%s";
    private static final String ECOURSE_URL_COURSE_LIST = "http://ecourse.elearning.ccu.edu.tw/php/Courses_Admin/take_course.php?frame=1";
    private static final String ECOURSE_URL_COURSE_SCORE = "http://ecourse.elearning.ccu.edu.tw/php/Trackin/SGQueryFrame1.php";
    private static final String ECOURSE_URL_COURSE_CLASSMATE = "http://ecourse.elearning.ccu.edu.tw/php/Learner_Profile/SSQueryFrame1.php";
    private static final String ECOURSE_URL_COURSE_ANNOUNCE = "http://ecourse.elearning.ccu.edu.tw/php/news/news.php";
    private static final String ECOURSE_URL_COURSE_FILELIST = "http://ecourse.elearning.ccu.edu.tw/php/textbook/course_menu.php?list=1";
    private static final String ECOURSE_URL_COURSE_FILELISTFILES = "http://ecourse.elearning.ccu.edu.tw/php/textbook/%s";
    private static final String ECOURSE_URL_COURSE_ANNOUNCE_CONTENT = "http://ecourse.elearning.ccu.edu.tw/php/news/%s";

    public EcourseRemoteSource(Ecourse ecourse, IParser parser) {
        this.ecourse = ecourse;
        this.parser = (EcourseParser) parser;
        this.auth = new CookieAuth();
        this.connectionHelper = new ConnectionHelper(auth);
    }

    public void setLocalStorage(EcourseLocalSource ecourseLocalSource) {
        this.ecourseLocalSource = ecourseLocalSource;
    }

    private void checkAuth() throws Exception {
        if(auth.getCookie(SESSION_FIELD_NAME) == null)
            if(sessionManager != null && !Authenticate(sessionManager)) {
                throw Exceptions.getLoginErrorException();
            };

    }

    public boolean Authenticate(String user, String pass) throws Exception {
        
        Connection connection = Jsoup.connect(ECOURSE_URL_LOGIN);
        ConnectionHelper.initTimeout(connection)
                .data("id", user)
                .data("pass", pass)
                .data("ver", "C");

        try {
            connection.post();
            String url = connection.response().url().toString();
            String body = connection.response().body();

            if (url.startsWith("http://ecourse.elearning.ccu.edu.tw/php/Courses_Admin/take_course.php")) {
                auth.setCookie(connection, SESSION_FIELD_NAME);
                return true;
            } else if (url.startsWith(ECOURSE_URL_LOGIN)) {
                if (body != null) {
                    if (body.contains("帳號或密碼錯誤")) {
                        throw new LoginErrorException("帳號或密碼錯誤");
                    }
                }
            }
            throw new LoginErrorException("未知錯誤");
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }

    @Override
    public boolean Authenticate(SessionManager sessionManager) throws Exception {
        if (!sessionManager.isLogined()) throw Exceptions.getNeedLoginException();

        return Authenticate(sessionManager.getUserName(), sessionManager.getPassword());
    }

    public boolean needSwitchCourse(Ecourse.Course course) {
        return currentCourse == null || (course != null && course.getCourseid() != null && !course.getCourseid().equals(currentCourse.getCourseid()));
    }

    @Override
    public void switchCourse(Ecourse.Course course) {
        if(!needSwitchCourse(course)) return;

        Connection connection;
        try {
            connection = Jsoup.connect(String.format(ECOURSE_URL_COURSE_SELECT, course.getCourseid()));
            connectionHelper.initConnection(connection).get();
            currentCourse = course;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Ecourse.Course[] getCourse() throws Exception {
        checkAuth();

        // reset
        currentCourse = null;

        Connection connection;

        connection = Jsoup.connect(ECOURSE_URL_COURSE_LIST);
        connectionHelper.initConnection(connection);

        try {
            Ecourse.Course[] result = parser.parserCourses(ecourse, connection.get());
            if(ecourseLocalSource != null) {
                ecourseLocalSource.storeCourse(result);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }

    @Override
    public Ecourse.Course[] getCourse(int year, int term, Kiki kiki) throws Exception {
        checkAuth();

        Ecourse.Course[] result;
        Kiki.Course[] courses;

        try {
            courses = kiki.getCourseList(year, term);
            result = new Ecourse.Course[courses.length];

            for (int i = 0; i < courses.length; i++) {
                result[i] = ecourse.new Course(ecourse);
                result[i].setCourseid(courses[i].getEcourseID());
                result[i].setName(courses[i].Name);
                result[i].setId("");
                result[i].setTeacher(courses[i].Teacher);
                result[i].setNotice(0);
                result[i].setHomework(0);
                result[i].setExam(0);
                result[i].setWarning(false);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

        return result;
    }

    public Ecourse.Scores[] getScore(Ecourse.Course course) throws Exception {
        checkAuth();

        Connection connection;
        connection = Jsoup.connect(ECOURSE_URL_COURSE_SCORE);
        connectionHelper.initConnection(connection);

        connection.method(Connection.Method.GET);

        try {
            //去避免亂碼問題
            Ecourse.Scores[] result = parser.parserScore(ecourse, Jsoup.parse(new String(connection.execute().bodyAsBytes(), "big5")));
            if(ecourseLocalSource != null) {
                ecourseLocalSource.storeScores(result, course);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

   }


    public Ecourse.Classmate[] getClassmate(Ecourse.Course course) throws Exception {
        checkAuth();

        Connection connection;
        connection = Jsoup.connect(ECOURSE_URL_COURSE_CLASSMATE);
        connectionHelper.initConnection(connection);

        try {
            return parser.parserClassmate(ecourse, connection.get());
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }

    public Ecourse.Announce[] getAnnounces(Ecourse.Course course) throws Exception {
        checkAuth();

        Connection connection;
        connection = Jsoup.connect(ECOURSE_URL_COURSE_ANNOUNCE);
        connectionHelper.initConnection(connection);

        try {
            Ecourse.Announce[] result = parser.parserAnnounce(course, connection.get());
            if(ecourseLocalSource != null) {
                ecourseLocalSource.storeAnnounce(result, course);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }

    public Ecourse.File[] getFiles(Ecourse.Course course) throws Exception {
        checkAuth();

        ArrayList<Ecourse.File> result = new ArrayList<Ecourse.File>();

        ecourse.switchCourse(course);
        getFileList(result);

        return result.toArray(new Ecourse.File[result.size()]);

    }

    private void getFileList(ArrayList<Ecourse.File> filelist) throws Exception {
        Connection connection;
        Document document;
        Elements lists;

        connection = Jsoup.connect(ECOURSE_URL_COURSE_FILELIST);
        connectionHelper.initConnection(connection);

        try {
            document = connection.get();
            lists = document.select("a[href^=course_menu.php]");

            for (int i = 0; i < lists.size(); i++) {
                getFileListFile(filelist, lists.get(i).attr("href"));
            }
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }

    private void getFileListFile(ArrayList<Ecourse.File> filelist, String href) {
        Connection connection;

        connection = Jsoup.connect(String.format(ECOURSE_URL_COURSE_FILELISTFILES, href));
        connectionHelper.initConnection(connection)
                .method(Connection.Method.GET);

        try {
            connection.execute();
            parser.parserFilesListFiles(
                    filelist,
                    connection.response().url().toString(),
                    ecourse,
                    Jsoup.parse(new String(connection.response().bodyAsBytes(), "big5"))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAnnounceContent(Ecourse.Announce announce) throws Exception {
        Connection connection;

        connection = Jsoup.connect(String.format(ECOURSE_URL_COURSE_ANNOUNCE_CONTENT, announce.url));
        connectionHelper.initConnection(connection);

        try {
            String result = parser.parserAnnounceContent(connection.get());
            if(ecourseLocalSource != null) {
                ecourseLocalSource.storeAnnounceContent(result, announce);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
