package com.netease.nim.samples.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Created by hzsunyj on 2019-06-03.
 */
public class PickUtil {

    public static final int REQUEST_IMAGE = 300;

    public static void choosePhoto(Activity context,int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        context.startActivityForResult(intent, requestCode);
    }

    public static void choosePhoto(Fragment context,int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        context.startActivityForResult(intent, requestCode);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getMediaPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = MediaStore.MediaColumns._ID + "=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String sanitizeFilename(String displayName) {
        String[] badCharacters = new String[] { "..", "/" };
        String[] segments = displayName.split("/");
        String fileName = segments[segments.length - 1];
        for (String suspString : badCharacters) {
            fileName = fileName.replace(suspString, "_");
        }
        return fileName;
    }

    public static File saferOpenFile (String path, String expectedDir) throws IllegalArgumentException, IOException {
        File f = new File(path);
        String canonicalPath = f.getCanonicalPath();
        if (!canonicalPath.startsWith(expectedDir)) {
            throw new IllegalArgumentException();
        }
        return f;
    }

    public static File getFile(final Context context, final Uri uri) {
        if(uri == null) {
            return null;
        }
        File file = null;
        //android10以上转换
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_FILE)) {
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                assert path != null;
                file = new File(path);
            }
        } else if (Objects.equals(uri.getScheme(), (ContentResolver.SCHEME_CONTENT))) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver == null) {
                return null;
            }
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex < 0) {
                return null;
            }
            String displayName = cursor.getString(nameIndex);
            cursor.close();
            String fileName = sanitizeFilename(displayName);
            try {
                ParcelFileDescriptor inputPFD = contentResolver.openFileDescriptor(uri, "r");
                if(inputPFD == null){
                    return null;
                }
                FileDescriptor fd = inputPFD.getFileDescriptor();
                FileInputStream is = new FileInputStream(fd);
                byte[] buffer = new byte[is.available()];
                int readLen = is.read(buffer);
                if (readLen != buffer.length) {
                    Log.w("PickUtil", "读取出的InputStream的长度和buffer不同");
                }

                String filePath =  new File(context.getCacheDir(), fileName).getPath();
                File outputFile = saferOpenFile(filePath, context.getCacheDir().getCanonicalPath());
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(buffer);
                file = outputFile;
                fos.close();
                is.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Get the value of the data column for this Uri . This is useful for
     * MediaStore Uris , and other file - based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public static MediaPlayer getMediaPlayer(Context context,File file) {
        try {
            return MediaPlayer.create(context, Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MediaPlayer getMediaPlayer(Context context,Uri uri) {
        try {
            return MediaPlayer.create(context,uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
