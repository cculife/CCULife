package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Charset;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.base.source.http.annotation.Url;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.File;
import org.zankio.ccudata.ecourse.model.FileGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Url(Urls.COURSE_FILELIST)
@Charset("big5")

@DataType(FileGroupSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class FileGroupSource extends EcourseSource<CourseData, FileGroup[]> {
    public final static String TYPE = "FILE_GROUP";

    public static Request<FileGroup[], CourseData> request(Course course) {
        return new Request<>(TYPE, new CourseData(course), FileGroup[].class);
    }

    @Override
    protected FileGroup[] parse(Request<FileGroup[], CourseData> request, HttpResponse response, Document document) throws Exception {
        List<FileGroup> fileGroupList = new ArrayList<>();

        Elements lists;
        File[] files;
        FileGroup mList;
        Course course = request.args.course;

        Repository context = getContext();
        lists = document.select("a[href^=course_menu.php], .child script");

        for (Element list : lists) {
            if (list.tag().getName().equals("a")) {
                files = context.fetch(FileGroupFilesSource.request(course, list.attr("href"))).toBlocking().last().data();
                if (files.length > 0) {
                    mList = new FileGroup();
                    mList.name = list.text();
                    mList.files = files;
                    fileGroupList.add(mList);
                }
            } else {
                Pattern pattern = Pattern.compile("href='(course_menu\\.php\\?.+)'>(.*?)<");
                Matcher matcher = pattern.matcher(list.html());
                while (matcher.find()) {
                    files = context.fetch(FileGroupFilesSource.request(course, matcher.group(1))).toBlocking().last().data();
                    if (files.length > 0) {
                        mList = new FileGroup();
                        mList.name = matcher.group(2).replaceAll("&lt;?", "<").replaceAll("&gt;?", ">").replaceAll("&nbsp;?", " ").replaceAll("&amp;?", "&");
                        mList.files = files;
                        fileGroupList.add(mList);
                    }
                }
            }
        }

        return fileGroupList.toArray(new FileGroup[fileGroupList.size()]);
    }
}
