package org.zankio.cculife.CCUService;

import android.content.Context;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

//Data : http://ecourse.elearning.ccu.edu.tw/
public class Ecourse extends BaseService{

    private Context context;

    public Course nowCourse = null;

    public Ecourse(Context context){
        this.context = context;
        this.SESSIONFIELDNAME = "PHPSESSID";
    }

    public void switchCourse(Course course) {
        if(nowCourse == null || !course.getCourseid().equals(nowCourse.getCourseid())) {
            nowCourse = course;
            try {
                Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/login_s.php?courseid=" + course.getCourseid())
                        .cookie(SESSIONFIELDNAME, SESSIONID).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkLoginSession(Document doc) {
        if (doc.title().equals("權限錯誤")) return false;
        return true;
    }

    @Override
    public boolean getSession() throws Exception {
        SessionManager sessionManager = SessionManager.getInstance(context);

        if (!sessionManager.isLogined()) throw Exceptions.getNeedLoginException();

        String user = sessionManager.getUserName();
        String pass = sessionManager.getPassword();
        Connection connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/index_login.php");
        connection.followRedirects(false)
                .data("id", user)
                .data("pass", pass)
                .data("ver", "C");
        try {
            connection.post();
            String Location = connection.response().header("Location");
            if (Location != null && Location.contains("take_course")) {
                SESSIONID = connection.response().cookie(SESSIONFIELDNAME);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw Exceptions.getNetworkException(e);
        }

    }

    public Course[] getCourses() throws Exception {
        if(SESSIONID == null) throw new Exception("登入錯誤");

        Course[] result = null;

        Connection connection;
        Document document;
        Elements courses, fields;

        connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/Courses_Admin/take_course.php?frame=1")
                     .cookie(SESSIONFIELDNAME, SESSIONID);

        try {
            /*
            ToDo Change the select funciont
            ToDo Check the select Node
             */

            document = connection.get();

            courses = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
            result = new Course[courses.size()];

            for (int i = 0; i < courses.size(); i++) {
                fields = courses.get(i).getElementsByTag("td");
                result[i] = new Course(this);

                result[i].setCourseid(fields.get(3).child(0).child(0).attr("href").replace("../login_s.php?courseid=", ""));
                result[i].setId(fields.get(2).text());
                result[i].setName(fields.get(3).text());
                result[i].setTeacher(fields.get(4).text());
                result[i].setNotice(Integer.parseInt(fields.get(5).text()));
                result[i].setHomework(Integer.parseInt(fields.get(6).text()));
                result[i].setExam(Integer.parseInt(fields.get(7).text()));
                result[i].setWarning(!fields.get(9).text().equals("--"));

            }

        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }

        return result;
    }

    public Course[] getCourses(int year, int term) throws Exception {
        if(SESSIONID == null) throw new Exception("登入錯誤");

        Course[] result;
        Kiki kiki;
        Kiki.Course[] courses;

        try {
            kiki = new Kiki(context);
            kiki.init();
            courses = kiki.getCourseList(year, term);
            result = new Course[courses.length];

            for (int i = 0; i < courses.length; i++) {
                result[i] = new Course(this);
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

    public class Course{
        private String courseid;
        private String id;
        private String name;
        private String teacher;
        private int notice;
        private int homework;
        private int exam;
        private boolean warning;
        private File[] files;
        private Announce[] announces;
        private Scores[] scores;
        private Ecourse ecourse;

        public Course(Ecourse content) {
            this.setEcourse(content);}

        public Course(String courseid, String id, String name, String teacher, int notice, int homework, int exam, String warning){
            this.setCourseid(courseid);
            this.setId(id);
            this.setName(name);
            this.setTeacher(teacher);
            this.setNotice(notice);
            this.setHomework(homework);
            this.setExam(exam);
            this.setWarning(!warning.equals("--"));
        }



        public Scores[] getScore() throws Exception {
            if (scores != null) return scores;

            Connection connection;
            Document document;
            Elements scores, fields;
            ArrayList<Scores> result;
            ArrayList<Score> score = null;
            Scores mScores = null;
            Score mScore = null;
            getEcourse().switchCourse(this);


            try {
                connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/Trackin/SGQueryFrame1.php")
                                  .cookie(SESSIONFIELDNAME, SESSIONID)
                                  .method(Connection.Method.GET);

                //去避免亂碼問題
                document = Jsoup.parse(new String(connection.execute().bodyAsBytes(), "big5"));

                scores = document.select("tr[bgcolor=#4d6eb2], tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");

                result = new ArrayList<Scores>();

                for(int i = 0; i < scores.size(); i++) {
                    fields = scores.get(i).select("td");

                    // Name row check
                    if ("#4d6eb2".equals(scores.get(i).attr("bgcolor"))) {
                        if (mScores != null && score.size() > 0) {
                            mScores.scores = score.toArray(new Score[score.size()]);
                            result.add(mScores);
                        }

                        mScores = new Scores();
                        mScores.Ｎame = fields.get(0).text().replace("(名稱)", "");
                        score = new ArrayList<Score>();
                        continue;
                    }

                    mScore = new Score();
                    mScore.Name = fields.get(0).text();
                    mScore.Percent = fields.get(1).text();
                    mScore.Score = fields.get(2).text();
                    mScore.Rank = fields.get(3).text();

                    assert score != null;
                    score.add(mScore);
                }

                assert score != null;
                if (score.size() > 0) {
                    mScores.scores = score.toArray(new Score[score.size()]);
                    result.add(mScores);
                }

                mScores = new Scores();
                mScores.Ｎame = "總分";
                scores = document.select("tr[bgcolor=#B0BFC3]");
                if (scores.size() >= 2) {
                    fields = scores.get(0).select("th");
                    if (fields.size() >= 2) mScores.Rank = fields.get(1).text();
                    fields = scores.get(1).select("th");
                    if (fields.size() >= 2) mScores.Score = fields.get(1).text();
                }

                if (mScores.Rank != null && !"你沒有成績".equals(mScores.Rank)) result.add(mScores);

                this.scores = result.toArray(new Scores[result.size()]);
                return this.scores;
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }
        }


        public Announce[] getAnnounces() throws Exception {
            if (this.announces != null) return this.announces;

            Connection connection;
            Document document;
            Elements announces, fields;
            Announce[] result;

            getEcourse().switchCourse(this);
            try {
                connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/news/news.php")
                                  .cookie(SESSIONFIELDNAME, SESSIONID);

                document = connection.get();

                announces = document.select("tr[bgcolor=#E6FFFC], tr[bgcolor=#F0FFEE]");
                result = new Announce[announces.size()];

                for(int i = 0; i < announces.size(); i++) {
                    fields = announces.get(i).select("td");

                    result[i] = new Announce(this.getEcourse(), this);
                    result[i].Date =  fields.get(0).text();
                    result[i].Title = fields.get(2).text();
                    result[i].important = fields.get(1).text();
                    result[i].browseCount = Integer.parseInt(fields.get(3).text());
                    result[i].isnew = fields.get(2).select("img").size() > 0;
                    result[i].url = fields.get(2).child(0).child(0).child(0).attr("onclick").split("'")[1].replace("./", "");
                }
                this.announces = result;
                return result;
            } catch (IOException e) {
                throw Exceptions.getNetworkException();
            }
        }

        public File[] getFiles() throws Exception {
            if (this.files != null) return this.files;

            ArrayList<File> result = new ArrayList<File>();

            getEcourse().switchCourse(this);
            getFileList(result);
            this.files = result.toArray(new File[result.size()]);

            return this.files;
        }

        private void getFileList(ArrayList<File> filelist) throws Exception {
            Connection connection;
            Document document;
            Elements lists;

            connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/textbook/course_menu.php?list=1")
                              .cookie(SESSIONFIELDNAME, SESSIONID);
            try {
                document = connection.get();
                lists = document.select("a[href^=course_menu.php]");

                for(int i = 0; i < lists.size(); i++) {
                    getFileListFile(filelist, lists.get(i).attr("href"));

                }
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }
        }

        private void getFileListFile(ArrayList<File> filelist, String href) {
            Connection connection;
            Document document;
            Elements files;
            Element nodeFile, nodeSize;
            File file;

            String baseurl, nodeHref;
            boolean standFileTemplate = false;

            connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/textbook/" + href);
            connection.cookie(SESSIONFIELDNAME, SESSIONID)
                      .method(Connection.Method.GET);

            try {
                document = Jsoup.parse(new String(connection.execute().bodyAsBytes(), "big5"));
                baseurl = connection.response().url().toString();

                if (baseurl.startsWith("http://ecourse.elearning.ccu.edu.tw/php/textbook/course_menu.php")) standFileTemplate = true;

                files = document.select("a");

                for (int i = 0; i < files.size(); i++) {
                    nodeFile = files.get(i);
                    nodeHref = nodeFile.attr("href");

                    if(nodeHref == null || nodeHref.equals("FILE_LINK") || nodeHref.startsWith("mailto:")) continue;

                    nodeHref = setBaseUrl(nodeHref, baseurl);

                    if (Pattern.matches("^http\\:\\/\\/ecourse\\.elearning\\.ccu\\.edu\\.tw\\/[^/]+\\/textbook\\/.+$", nodeHref)) {

                        file = new File();
                        file.Name = getFileName(nodeHref);
                        file.URL = nodeHref;

                        if (standFileTemplate) {
                            nodeSize = nodeFile.parent().nextElementSibling();
                            file.Name = nodeFile.text();
                            file.Size = nodeSize.text();
                        }

                        filelist.add(file);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getFileName(String url) {
            url = url.substring(url.lastIndexOf('/') + 1);
            try {
                return java.net.URLDecoder.decode(url, "big5");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return url;
        }

        private String setBaseUrl(String url, String base) {
            if(url == null ||
                    url.startsWith("http://") ||
                    url.startsWith("https://") ||
                    url.startsWith("ftp://") ||
                    url.startsWith("mailto:")
              ) return url;

            URL mUrl = null;
            try {
                mUrl = new URL(base);
                return mUrl.getProtocol() + "://" + mUrl.getHost() + mUrl.getFile().substring(0, mUrl.getFile().lastIndexOf('/') + 1) + url;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return url;
        }

        public String getCourseid() {
            return courseid;
        }

        public void setCourseid(String courseid) {
            this.courseid = courseid;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTeacher() {
            return teacher;
        }

        public void setTeacher(String teacher) {
            this.teacher = teacher;
        }

        public int getNotice() {
            return notice;
        }

        public void setNotice(int notice) {
            this.notice = notice;
        }

        public int getHomework() {
            return homework;
        }

        public void setHomework(int homework) {
            this.homework = homework;
        }

        public int getExam() {
            return exam;
        }

        public void setExam(int exam) {
            this.exam = exam;
        }

        public boolean isWarning() {
            return warning;
        }

        public void setWarning(boolean warning) {
            this.warning = warning;
        }

        public Ecourse getEcourse() {
            return ecourse;
        }

        public void setEcourse(Ecourse ecourse) {
            this.ecourse = ecourse;
        }

        public void setFiles(File[] files) {
            this.files = files;
        }

        public void setAnnounces(Announce[] announces) {
            this.announces = announces;
        }

        public Scores[] getScores() {
            return scores;
        }

    }

    public class File{
        public String Name;
        public String URL;
        public String Size;
    }

    public class Announce {
        public String url;
        public String Date;
        public String Title;
        public String Content = null;
        public String important;
        public int browseCount;
        public boolean isnew;
        protected Ecourse ecourse;
        protected Course course;

        public Announce(Ecourse ecourse, Course course) {this.ecourse = ecourse; this.course = course;}

        public String getContent() {
            if (Content != null) return Content;
            Connection connection;
            Document document;
            Elements rows;
            ecourse.switchCourse(course);
            connection = Jsoup.connect("http://ecourse.elearning.ccu.edu.tw/php/news/" + url)
                              .cookie(SESSIONFIELDNAME, SESSIONID);
            try {
                document = connection.get();
                rows = document.select("td[bgcolor=#E8E8E8]");

                if (rows.size() > 2 )
                    Content = rows.get(2).html();
                else
                    Content = "資料讀取錯誤";

            } catch (IOException e) {
                e.printStackTrace();
                Content = Exceptions.getNetworkErrorMessage();
            }

            return Content;
        }
    }

    public class Scores {
        public Score[] scores;
        public String Ｎame;
        public String Score;
        public String Rank;
    }

    public class Score {
        public String Name;
        public String Score;
        public String Rank;
        public String Percent;
    }
}
