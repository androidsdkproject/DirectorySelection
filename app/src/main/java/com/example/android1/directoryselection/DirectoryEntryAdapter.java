

package com.example.android1.directoryselection;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class DirectoryEntryAdapter extends RecyclerView.Adapter<DirectoryEntryAdapter.ViewHolder> {

    static final String DIRECTORY_MIME_TYPE = "vnd.android.document/directory";
    private List<DirectoryEntry> mDirectoryEntries;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mFileName;
        private final TextView mMimeType;
        private final ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mFileName = (TextView) v.findViewById(R.id.textview_filename);
            mMimeType = (TextView) v.findViewById(R.id.textview_mimetype);
            mImageView = (ImageView) v.findViewById(R.id.entry_image);
        }

        public TextView getFileName() {
            return mFileName;
        }

        public TextView getMimeType() {
            return mMimeType;
        }

        public ImageView getImageView() {
            return mImageView;
        }
    }

    public DirectoryEntryAdapter(List<DirectoryEntry> directoryEntries) {
        mDirectoryEntries = directoryEntries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.directory_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getFileName().setText(mDirectoryEntries.get(position).fileName);
        viewHolder.getMimeType().setText(mDirectoryEntries.get(position).mimeType);

        if (DIRECTORY_MIME_TYPE.equals(mDirectoryEntries.get(position).mimeType)) {
            viewHolder.getImageView().setImageResource(R.drawable.ic_folder_grey600_36dp);
        } else {
            viewHolder.getImageView().setImageResource(R.drawable.ic_description_grey600_36dp);
        }
    }

    @Override
    public int getItemCount() {
        return mDirectoryEntries.size();
    }

    public void setDirectoryEntries(List<DirectoryEntry> directoryEntries) {
        mDirectoryEntries = directoryEntries;
    }
}
