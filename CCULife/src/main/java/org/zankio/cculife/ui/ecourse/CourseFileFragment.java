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
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.File;
import org.zankio.cculife.CCUService.ecourse.model.FileGroup;
import org.zankio.cculife.R;
import org.zankio.cculife.Utils;
import org.zankio.cculife.services.DownloadService;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;

import java.util.ArrayList;
import java.util.List;

public class CourseFileFragment extends BaseMessageFragment
        implements ExpandableListView.OnChildClickListener, IOnUpdateListener<FileGroup[]> {
    private List<File> download_list;
    private Course course;
    private FileAdapter adapter;
    private ExpandableListView list;
    private boolean loading;
    private IGetCourseData context;

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

        loading = course.getFiles(this);

        if (loading)
            showMessage("讀取中...", true);
    }

    @Override
    public void onNext(String type, FileGroup[] fileGroups, BaseSource source) {
        this.loading = false;
        onFileUpdate(fileGroups);
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        this.loading = false;
        showMessage(err.getMessage());
    }

    @Override
    public void onComplete(String type) {

    }

    private void onFileUpdate(FileGroup[] fileGroups) {
        if (fileGroups == null || fileGroups.length == 0) {
            showMessage("沒有檔案");
            return;
        }

        adapter.setFiles(fileGroups);
        if (fileGroups.length == 1) {
            list.setGroupIndicator(null);
            list.expandGroup(0);

        }
        hideMessage();

    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        File file;
        String filename;
        file = (File) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
        assert file != null;
        if (Utils.checkWritePermission(getParentFragment())) {
            filename = file.name != null ? file.name : URLUtil.guessFileName(file.url, null, null);
            DownloadService.downloadFile(getContext(), file.url, filename);

            Toast.makeText(getContext(), "下載 : " + filename, Toast.LENGTH_SHORT).show();
        } else {
            if (download_list == null) download_list = new ArrayList<>();
            download_list.add(file);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (download_list == null) return;
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "沒有儲存權限!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String filename;

                for (File file : download_list) {
                    filename = file.name != null ? file.name : URLUtil.guessFileName(file.url, null, null);
                    DownloadService.downloadFile(getContext(), file.url, filename);
                    Toast.makeText(getContext(), "下載 : " + filename, Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    }
    public class FileAdapter extends BaseExpandableListAdapter {
        private FileGroup[] filelists;
        private LayoutInflater inflater;

        public FileAdapter() {
            this.inflater = LayoutInflater.from(getContext());
        }

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
            FileGroup group = (FileGroup) getGroup(groupPosition);
            View view;

            if (convertView == null) view = inflater.inflate(R.layout.item_file_group, null);
            else view = convertView;

            ((TextView) view.findViewById(R.id.Name)).setText(group.name);
            if (getGroupCount() == 1)
                view.setLayoutParams(new AbsListView.LayoutParams(1, 1));
            else
                view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            File file = (File) getChild(groupPosition, childPosition);
            View view;

            if (convertView == null) view = inflater.inflate(R.layout.item_file, null);
            else view = convertView;

            ((TextView) view.findViewById(R.id.Name)).setText(file.name);
            ((TextView) view.findViewById(R.id.Size)).setText(file.size != null ? file.size : "");

            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
