package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.File;
import org.zankio.cculife.CCUService.ecourse.model.FileGroup;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileGroupSource extends BaseSource<Void> {
    public final static String TYPE = "FILE_GROUP";
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

    public FileGroupSource(Ecourse context) {
        super(context, property);
    }

    //Call From FileSource
    @Override
    public Void fetch(String type, Object... arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg is miss");

        Ecourse context = (Ecourse) this.context;
        ArrayList<FileGroup> filelist = (ArrayList<FileGroup>) arg[1];

        try {
            Connection connection;
            Document document;
            Elements lists;
            ArrayList<File> files;
            FileGroup mList;

            connection = context.buildConnection(Url.COURSE_FILELIST);

            document = connection.get();
            lists = document.select("a[href^=course_menu.php], .child script");

            for (Element list : lists)
                if (list.tag().getName().equals("a")) {
                    files = new ArrayList<>();
                    context.fetchSync(FileGroupFilesSource.TYPE, files, list.attr("href"));
                    if (files.size() > 0) {
                        mList = new FileGroup();
                        mList.name = list.text();
                        mList.files = files.toArray(new File[files.size()]);
                        filelist.add(mList);
                    }
                } else {
                    Pattern pattern = Pattern.compile("href='(course_menu\\.php\\?.+)'>(.*?)<");
                    Matcher matcher = pattern.matcher(list.html());
                    while (matcher.find()) {
                        files = new ArrayList<>();
                        context.fetchSync(FileGroupFilesSource.TYPE, files, matcher.group(1));
                        if (files.size() > 0) {
                            mList = new FileGroup();
                            mList.name = matcher.group(2).replaceAll("&lt;?", "<").replaceAll("&gt;?", ">").replaceAll("&nbsp;?", " ").replaceAll("&amp;?", "&");
                            mList.files = files.toArray(new File[files.size()]);
                            filelist.add(mList);
                        }
                    }
                }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}
