package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.File;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FileGroupFilesSource extends BaseSource<Void> {
    public final static String TYPE = "FILE_GROUP_FILES";
    public final static String[] DATA_TYPES = { TYPE };
    public final static SourceProperty property;
    static  {
        property = new SourceProperty(
                SourceProperty.Level.MIDDLE,
                SourceProperty.Level.HIGH,
                false,
                DATA_TYPES
        );
    }

    public FileGroupFilesSource(Ecourse context) {
        super(context, property);
    }

    public void parseFilesListFiles(ArrayList<File> filelist, String baseurl, Document document) {
        Elements files;
        Element nodeFile, nodeSize;
        File file;

        String nodeHref;
        boolean standFileTemplate = false;

        if (baseurl.startsWith("http://ecourse.ccu.edu.tw/php/textbook/course_menu.php")) standFileTemplate = true;
        if (baseurl.startsWith("https://ecourse.ccu.edu.tw/php/textbook/course_menu.php")) standFileTemplate = true;

        files = document.select("a");

        for (int i = 0; i < files.size(); i++) {
            nodeFile = files.get(i);
            nodeHref = nodeFile.attr("href");

            if(nodeHref == null || nodeHref.equals("FILE_LINK") || nodeHref.startsWith("mailto:")) continue;

            nodeHref = setBaseUrl(nodeHref, baseurl);

            if (Pattern.matches("^https?\\:\\/\\/ecourse(?:\\.elearning)?\\.ccu\\.edu\\.tw\\/[^/]+\\/textbook\\/.+$", nodeHref)) {

                file = new File();
                file.name = getFileName(nodeHref);
                file.url = nodeHref;

                if (standFileTemplate) {
                    nodeSize = nodeFile.parent().nextElementSibling();
                    file.name = nodeFile.text();
                    file.size = nodeSize.text();
                }

                filelist.add(file);
            }
        }
    }


    private String getFileName(String url) {
        url = url.substring(url.lastIndexOf('/') + 1);
        try {
            url = java.net.URLDecoder.decode(url, "ISO-8859-1");
            return new String(url.getBytes("ISO-8859-1"), "big5");
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

        URL mUrl;
        try {
            mUrl = new URL(base);
            return mUrl.getProtocol() + "://" + mUrl.getHost() + mUrl.getFile().substring(0, mUrl.getFile().lastIndexOf('/') + 1) + url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    @Override
    public Void fetch(String type, Object... arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg is miss");

        Ecourse context = (Ecourse) this.context;
        BaseSession session = context.getSession();
        ArrayList<File> filelist = (ArrayList<File>) arg[0];
        String href = (String) arg[1];
        if (session == null) throw new Exception("Session is miss");

        CourseSource.authenticate(context, session);

        try {
            Connection connection;

            connection = context.buildConnection(String.format(Url.COURSE_FILELISTFILES, href))
                    .method(Connection.Method.GET);

            connection.execute();
            parseFilesListFiles(
                    filelist,
                    connection.response().url().toString(),
                    Jsoup.parse(new String(connection.response().bodyAsBytes(), "big5"))
            );
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}
