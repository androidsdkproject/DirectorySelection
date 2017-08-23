package com.example.android1.directoryselection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DirectorySelectionFragment extends Fragment {

    private static final String TAG = DirectorySelectionFragment.class.getSimpleName();

    public static final int REQUEST_CODE_OPEN_DIRECTORY = 1;

    Uri mCurrentDirectoryUri;
    TextView mCurrentDirectoryTextView;
    Button mCreateDirectoryButton;
    RecyclerView mRecyclerView;
    DirectoryEntryAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    public static DirectorySelectionFragment newInstance() {
        DirectorySelectionFragment fragment = new DirectorySelectionFragment();
        return fragment;
    }

    public DirectorySelectionFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_directory_selection, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        rootView.findViewById(R.id.button_open_directory)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
                    }
                });

        mCurrentDirectoryTextView = (TextView) rootView
                .findViewById(R.id.textview_current_directory);
        mCreateDirectoryButton = (Button) rootView.findViewById(R.id.button_create_directory);
        mCreateDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editView = new EditText(getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.create_directory)
                        .setView(editView)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        createDirectory(mCurrentDirectoryUri,
                                                editView.getText().toString());
                                        updateDirectoryEntries(mCurrentDirectoryUri);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                        .show();
            }
        });
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_directory_entries);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mAdapter = new DirectoryEntryAdapter(new ArrayList<DirectoryEntry>());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, String.format("Open Directory result Uri : %s", data.getData()));
            updateDirectoryEntries(data.getData());
            mAdapter.notifyDataSetChanged();
        }
    }

    void updateDirectoryEntries(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri docUri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
        }
        Uri childrenUri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
        }

        Cursor docCursor = contentResolver.query(docUri, new String[]{
                Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE}, null, null, null);
        try {
            while (docCursor.moveToNext()) {
                Log.d(TAG, "found doc =" + docCursor.getString(0) + ", mime=" + docCursor
                        .getString(1));
                mCurrentDirectoryUri = uri;
                mCurrentDirectoryTextView.setText(docCursor.getString(0));
                mCreateDirectoryButton.setEnabled(true);
            }
        } finally {
            closeQuietly(docCursor);
        }

        Cursor childCursor = contentResolver.query(childrenUri, new String[]{
                Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE}, null, null, null);
        try {
            List<DirectoryEntry> directoryEntries = new ArrayList<>();
            while (childCursor.moveToNext()) {
                Log.d(TAG, "found child=" + childCursor.getString(0) + ", mime=" + childCursor
                        .getString(1));
                DirectoryEntry entry = new DirectoryEntry();
                entry.fileName = childCursor.getString(0);
                entry.mimeType = childCursor.getString(1);
                directoryEntries.add(entry);
            }
            mAdapter.setDirectoryEntries(directoryEntries);
            mAdapter.notifyDataSetChanged();
        } finally {
            closeQuietly(childCursor);
        }
    }


    void createDirectory(Uri uri, String directoryName) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri docUri = null;
        Uri directoryUri = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                        DocumentsContract.getTreeDocumentId(uri));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                directoryUri = DocumentsContract
                        .createDocument(contentResolver, docUri, Document.MIME_TYPE_DIR, directoryName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (directoryUri != null) {
            Log.i(TAG, String.format(
                    "Created directory : %s, Document Uri : %s, Created directory Uri : %s",
                    directoryName, docUri, directoryUri));
            Toast.makeText(getActivity(), String.format("Created a directory [%s]",
                    directoryName), Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, String.format("Failed to create a directory : %s, Uri %s", directoryName,
                    docUri));
            Toast.makeText(getActivity(), String.format("Failed to created a directory [%s] : ",
                    directoryName), Toast.LENGTH_SHORT).show();
        }

    }

    public void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    closeable.close();
                }
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }
}

