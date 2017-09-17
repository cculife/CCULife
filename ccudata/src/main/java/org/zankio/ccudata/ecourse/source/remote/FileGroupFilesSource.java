package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Charset;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.utils.FileUtils;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.File;
import org.zankio.ccudata.ecourse.model.FileGroupData;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Charset("big5")

@DataType(FileGroupFilesSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
public class FileGroupFilesSource extends EcourseSource<FileGroupData, File[]> {
    public final static String TYPE = "FILE_GROUP_FILES";

    public static Request<File[], FileGroupData> request(Course course, String href) {
        return new Request<>(TYPE, new FileGroupData(href, course), File[].class);
    }

    @Override
    public void initHTTPRequest(Request<File[], FileGroupData> request) {
        super.initHTTPRequest(request);
        FileGroupData fileGroupData = request.args;
        httpParameter(request).url(String.format(Urls.COURSE_FILELISTFILES, fileGroupData.href));
    }

    @Override
    protected File[] parse(Request<File[], FileGroupData> request, HttpResponse response, Document document) throws Exception {
        ArrayList<File> fileGroupList = new ArrayList<>();
        String baseurl = response.url();

        Elements files;
        Element nodeFile, nodeSize;
        Element nodeDate;
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
                    nodeDate = nodeSize.nextElementSibling();
                    file.name = nodeFile.text();
                    file.date = nodeDate.text();
                    try {
                        file.size = FileUtils.humanReadableByteCount(Long.valueOf(nodeSize.text()), true);
                    } catch (NumberFormatException e) {
                        file.size = nodeSize.text();
                    }

                }

                fileGroupList.add(file);
            }
        }
        return fileGroupList.toArray(new File[fileGroupList.size()]);
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
}
