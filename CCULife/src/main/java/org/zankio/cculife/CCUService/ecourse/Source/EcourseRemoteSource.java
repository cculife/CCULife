package org.zankio.cculife.CCUService.ecourse.source;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.authentication.CookieAuth;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.base.helper.ConnectionHelper;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.CCUService.kiki.Kiki;
import org.zankio.cculife.CCUService.ecourse.parser.EcourseParser;
import org.zankio.cculife.CCUService.base.parser.IParser;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;
import org.zankio.cculife.override.LoginErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EcourseRemoteSource extends EcourseSource {
    private static final String SESSION_FIELD_NAME = "PHPSESSID";

    private Ecourse ecourse;
    private Ecourse.Course currentCourse;
    private ConnectionHelper connectionHelper;
    private CookieAuth auth;
    private EcourseParser parser;
    private EcourseLocalSource ecourseLocalSource;
    private SessionManager sessionManager;


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

        Connection connection = Jsoup.connect(Url.LOGIN);
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
            } else if (url.startsWith(Url.LOGIN)) {
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
            connection = Jsoup.connect(String.format(Url.COURSE_SELECT, course.getCourseid()));
            connectionHelper.init(connection).get();
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

        connection = connectionHelper.create(Url.COURSE_LIST);

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
        connection = connectionHelper.create(Url.COURSE_SCORE);

        connection.method(Connection.Method.GET);

        try {
            //去避免亂碼問題
            Ecourse.Scores[] result = parser.parserScore(
                    ecourse,
                    Jsoup.parse(new String(connection.execute().bodyAsBytes(), "big5"))
            );
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
        connection = connectionHelper.create(Url.COURSE_CLASSMATE);

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
        connection = connectionHelper.create(Url.COURSE_ANNOUNCE);

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

    public Ecourse.FileList[] getFiles(Ecourse.Course course) throws Exception {
        checkAuth();

        ArrayList<Ecourse.FileList> result = new ArrayList<Ecourse.FileList>();

        ecourse.switchCourse(course);
        getFileList(result);

        return result.toArray(new Ecourse.FileList[result.size()]);

    }

    private void getFileList(ArrayList<Ecourse.FileList> filelist) throws Exception {
        Connection connection;
        Document document;
        Elements lists;
        ArrayList<Ecourse.File> files;
        Ecourse.FileList mList;

        connection = connectionHelper.create(Url.COURSE_FILELIST);

        try {
            document = connection.get();
            lists = document.select("a[href^=course_menu.php], .child script");

            for (Element list : lists) {
                if (list.tag().getName().equals("a")) {
                    files = new ArrayList<Ecourse.File>();
                    getFileListFile(files, list.attr("href"));
                    if (files.size() > 0) {
                        mList = ecourse.new FileList();
                        mList.Name = list.text();
                        mList.Files = files.toArray(new Ecourse.File[files.size()]);
                        filelist.add(mList);
                    }
                }
                else {
                    Pattern pattern = Pattern.compile("href='(course_menu\\.php\\?.+)'>(.*?)<");
                    Matcher matcher = pattern.matcher(list.html());
                    while(matcher.find()) {
                        files = new ArrayList<Ecourse.File>();
                        getFileListFile(files, matcher.group(1));
                        if (files.size() > 0) {
                            mList = ecourse.new FileList();
                            mList.Name = matcher.group(2).replaceAll("&lt;?", "<").replaceAll("&gt;?", ">").replaceAll("&nbsp;?", " ").replaceAll("&amp;?", "&");
                            mList.Files = files.toArray(new Ecourse.File[files.size()]);
                            filelist.add(mList);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }
    }

    private void getFileListFile(ArrayList<Ecourse.File> filelist, String href) {
        Connection connection;

        connection = connectionHelper.create(String.format(Url.COURSE_FILELISTFILES, href))
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

        connection = connectionHelper.create(String.format(Url.COURSE_ANNOUNCE_CONTENT, announce.url));

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
