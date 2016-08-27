package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.File;
import org.zankio.ccudata.ecourse.model.FileGroup;
import org.zankio.cculife.R;
import org.zankio.cculife.utils.PermissionUtils;
import org.zankio.cculife.services.DownloadService;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;
import org.zankio.cculife.utils.ExceptionUtils;
import org.zankio.cculife.utils.UnitUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;


public class CourseFileFragment extends BaseMessageFragment
        implements ExpandableListView.OnChildClickListener, IGetLoading {
    private List<File> download_list;
    private Course course;
    private FileAdapter adapter;
    private ExpandableListView list;
    private boolean loaded;
    private IGetCourseData context;
    private CourseFragment.LoadingListener loadedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IGetCourseData");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_file, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new FileAdapter();
        list = (ExpandableListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnChildClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        courseChange(getArguments().getString("id"));
    }

    public void courseChange(String id) {
        course = context.getCourse(id);
        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        course.getFiles()
                .subscribe(new Subscriber<Response<FileGroup[], CourseData>>() {
                    private boolean noData = true;
                    @Override
                    public void onStart() {
                        super.onStart();
                        setLoaded(false);
                        message().show("讀取中...", true);
                    }

                    @Override
                    public void onCompleted() {
                        if (noData)
                            message().show("沒有檔案");

                        setLoaded(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e = ExceptionUtils.extraceException(e);

                        setLoaded(true);
                        message().show(e.getMessage());
                    }

                    @Override
                    public void onNext(Response<FileGroup[], CourseData> courseDataResponse) {
                        FileGroup[] fileGroups = courseDataResponse.data();

                        if (fileGroups == null || fileGroups.length == 0) {
                            return;
                        }

                        adapter.setFiles(fileGroups);
                        if (fileGroups.length == 1) {
                            list.setGroupIndicator(null);
                            list.expandGroup(0);

                        }

                        noData = false;
                        message().hide();
                    }
                });
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        File file;
        String filename;
        file = (File) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
        assert file != null;

        // check permission
        if (PermissionUtils.checkWritePermission(getParentFragment())) {

            // guess file name
            filename = file.name != null ? file.name : URLUtil.guessFileName(file.url, null, null);

            // start download
            DownloadService.downloadFile(getContext(), file.url, filename);

            Toast.makeText(getContext(), "下載 : " + filename, Toast.LENGTH_SHORT).show();
        } else {

            // can't get permission
            if (download_list == null) download_list = new ArrayList<>();
            download_list.add(file);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (download_list == null) return;

                // get write external storage fail
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "沒有儲存權限!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String filename;

                // restart download
                for (File file : download_list) {
                    filename = file.name != null ? file.name : URLUtil.guessFileName(file.url, null, null);
                    DownloadService.downloadFile(getContext(), file.url, filename);
                    Toast.makeText(getContext(), "下載 : " + filename, Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
        if (loadedListener != null) loadedListener.call(loaded);
    }

    @Override
    public boolean isLoading() {
        return !this.loaded;
    }

    @Override
    public void setLoadedListener(CourseFragment.LoadingListener listener) {
        this.loadedListener = listener;
    }

    public class FileAdapter extends BaseExpandableListAdapter {
        private FileGroup[] filelists;

        public void setFiles(FileGroup[] filelists) {
            this.filelists = filelists;
            this.notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return filelists == null ? 0 : filelists.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return filelists[groupPosition].files.length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return filelists[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return filelists[groupPosition].files[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if (convertView == null) convertView = inflater.inflate(R.layout.item_file_group, parent, false);

            FileGroup group = (FileGroup) getGroup(groupPosition);

            ((TextView) convertView.findViewById(R.id.Name)).setText(group.name);

            // hide group when only one group
            if (getGroupCount() == 1)
                convertView.setLayoutParams(new AbsListView.LayoutParams(1, 1));
            else
                convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            File file = (File) getChild(groupPosition, childPosition);

            if (convertView == null) convertView = inflater.inflate(R.layout.item_file, parent, false);

            StringBuilder description = new StringBuilder();
            if (file.date != null)
                description.append(file.date.substring(0, 10));

            if (file.size != null)
                if (description.length() != 0) description.append(" / ");
                description.append(file.size);

            ((TextView) convertView.findViewById(R.id.Name)).setText(file.name);
            ((TextView) convertView.findViewById(R.id.Size)).setText(description);

            if (getGroupCount() == 1)
                convertView.findViewById(R.id.padding).setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
            else
                convertView.findViewById(R.id.padding).setLayoutParams(new RelativeLayout.LayoutParams(UnitUtils.getDp(getContext(), 40), 0));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
