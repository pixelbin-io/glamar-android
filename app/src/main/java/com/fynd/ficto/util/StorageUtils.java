package com.fynd.ficto.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StorageUtils {
	private static final String TAG = "StorageUtils";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    boolean showlog=true;

	Context context = null;
    private Uri last_media_scanned = null;

	// for testing:
	public boolean failed_to_scan = false;
	public Uri videoUri;
	
	public StorageUtils(Context context) {
		this.context = context;
	}
	
	public Uri getLastMediaScanned() {
		return last_media_scanned;
	}
	public void clearLastMediaScanned() {
		last_media_scanned = null;
	}

	public void announceUri(Uri uri, boolean is_new_picture, boolean is_new_video) {

		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(uri);
		context.sendBroadcast(mediaScanIntent);

		if (is_new_picture) {

			// note, we reference the string directly rather than via Camera.ACTION_NEW_PICTURE, as the latter class is now deprecated - but we still need to broadcase the string for other apps
			context.sendBroadcast(new Intent("android.hardware.action.NEW_PICTURE", uri));
			// for compatibility with some apps - apparently this is what used to be broadcast on Android?
			context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));

		/*	if (NeonDebug.LOG) // this code only used for debugging/logging
			{
				String[] CONTENT_PROJECTION = {Images.Media.DATA, Images.Media.DISPLAY_NAME, Images.Media.MIME_TYPE, Images.Media.SIZE, Images.Media.DATE_TAKEN, Images.Media.DATE_ADDED};
				Cursor c = context.getContentResolver().query(uri, CONTENT_PROJECTION, null, null, null);
				if (c == null) {
					if (NeonDebug.LOG)
						Log.e(TAG, "Couldn't resolve given uri [1]: " + uri);
				} else if (!c.moveToFirst()) {
					if (NeonDebug.LOG)
						Log.e(TAG, "Couldn't resolve given uri [2]: " + uri);
				} else {
					String file_path = c.getString(c.getColumnIndex(Images.Media.DATA));
					String file_name = c.getString(c.getColumnIndex(Images.Media.DISPLAY_NAME));
					String mime_type = c.getString(c.getColumnIndex(Images.Media.MIME_TYPE));
					long date_taken = c.getLong(c.getColumnIndex(Images.Media.DATE_TAKEN));
					long date_added = c.getLong(c.getColumnIndex(Images.Media.DATE_ADDED));
				*//*	Log.d(TAG, "file_path: " + file_path);
					Log.d(TAG, "file_name: " + file_name);
					Log.d(TAG, "mime_type: " + mime_type);
					Log.d(TAG, "date_taken: " + date_taken);
					Log.d(TAG, "date_added: " + date_added);*//*
					c.close();
				}
			}*/
 			/*{
 				// hack: problem on Camera2 API (at least on Nexus 6) that if geotagging is enabled, then the resultant image has incorrect Exif TAG_GPS_DATESTAMP (GPSDateStamp) set (tends to be around 2038 - possibly a driver bug of casting long to int?)
 				// whilst we don't yet correct for that bug, the more immediate problem is that it also messes up the DATE_TAKEN field in the media store, which messes up Gallery apps
 				// so for now, we correct it based on the DATE_ADDED value.
    	        String[] CONTENT_PROJECTION = { Images.Media.DATE_ADDED }; 
    	        Cursor c = context.getContentResolver().query(uri, CONTENT_PROJECTION, null, null, null); 
    	        if( c == null ) { 
		 			if( NeonDebug.LOG )
		 				Log.e(TAG, "Couldn't resolve given uri [1]: " + uri); 
    	        }
    	        else if( !c.moveToFirst() ) { 
		 			if( NeonDebug.LOG )
		 				Log.e(TAG, "Couldn't resolve given uri [2]: " + uri); 
    	        }
    	        else {
        	        long date_added = c.getLong(c.getColumnIndex(Images.Media.DATE_ADDED)); 
		 			if( NeonDebug.LOG )
		 				Log.e(TAG, "replace date_taken with date_added: " + date_added); 
					ContentValues values = new ContentValues(); 
					values.put(Images.Media.DATE_TAKEN, date_added*1000); 
					context.getContentResolver().update(uri, values, null, null);
        	        c.close(); 
    	        }
 			}*/
		} else if (is_new_video) {
			context.sendBroadcast(new Intent("android.hardware.action.NEW_VIDEO", uri));

    		/*String[] CONTENT_PROJECTION = { Video.Media.DURATION }; 
			Cursor c = context.getContentResolver().query(uri, CONTENT_PROJECTION, null, null, null);
	        if( c == null ) { 
	 			if( NeonDebug.LOG )
	 				Log.e(TAG, "Couldn't resolve given uri [1]: " + uri); 
	        }
	        else if( !c.moveToFirst() ) { 
	 			if( NeonDebug.LOG )
	 				Log.e(TAG, "Couldn't resolve given uri [2]: " + uri); 
	        }
	        else {
    	        long duration = c.getLong(c.getColumnIndex(Video.Media.DURATION)); 
	 			if( NeonDebug.LOG )
	 				Log.e(TAG, "replace duration: " + duration); 
				ContentValues values = new ContentValues(); 
				values.put(Video.Media.DURATION, 1000); 
				context.getContentResolver().update(uri, values, null, null);
    	        c.close(); 
	        }*/
		}
	}
	
    public void broadcastFile(final File file, final boolean is_new_picture, final boolean is_new_video) {
		// note that the new method means that the new folder shows up as a file when connected to a PC via MTP (at least tested on Windows 8)
		if (file.isDirectory()) {
			//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
			// ACTION_MEDIA_MOUNTED no longer allowed on Android 4.4! Gives: SecurityException: Permission Denial: not allowed to send broadcast android.intent.action.MEDIA_MOUNTED
			// note that we don't actually need to broadcast anything, the folder and contents appear straight away (both in Gallery on device, and on a PC when connecting via MTP)
			// also note that we definitely don't want to broadcast ACTION_MEDIA_SCANNER_SCAN_FILE or use scanFile() for folders, as this means the folder shows up as a file on a PC via MTP (and isn't fixed by rebooting!)
		} else {
			// both of these work fine, but using MediaScannerConnection.scanFile() seems to be preferred over sending an intent
			//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
			failed_to_scan = true; // set to true until scanned okay
			MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							failed_to_scan = false;
							last_media_scanned = uri;
							announceUri(uri, is_new_picture, is_new_video);
							Log.d("diiff"," uri "+uri+" path "+path);

							// it seems caller apps seem to prefer the content:// Uri rather than one based on a File
							Activity activity = (Activity) context;
							String action = activity.getIntent().getAction();
							if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
								Intent output = new Intent();
								output.setData(uri);
								activity.setResult(Activity.RESULT_OK, output);
								activity.finish();
							}
						}
					}
			);

		}
	}

	public Uri getURI(){
		return videoUri;
	}
	public void getVideoURI(final File file, final boolean is_new_picture, final boolean is_new_video, final SharedPreferences sharedPreferences) {

		// note that the new method means that the new folder shows up as a file when connected to a PC via MTP (at least tested on Windows 8)

		// both of these work fine, but using MediaScannerConnection.scanFile() seems to be preferred over sending an intent
		//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
		failed_to_scan = true; // set to true until scanned okay
		MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						failed_to_scan = false;

						videoUri = uri;
						announceUri(uri, is_new_picture, is_new_video);



						sharedPreferences.edit().putString("lastImageUri",uri.toString());
						String val= sharedPreferences.getString("lastImageUri"," no ");
						Log.d("diiff"," uri "+uri+" val "+videoUri+" path "+path);

						// it seems caller apps seem to prefer the content:// Uri rather than one based on a File
						Activity activity = (Activity) context;
						String action = activity.getIntent().getAction();
						if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
							Intent output = new Intent();
							output.setData(uri);
							activity.setResult(Activity.RESULT_OK, output);
							activity.finish();
						}
					}
				}
		);
		Log.d("diiff"," uri val "+videoUri);



	}


	public boolean isUsingSAF() {
    	// check Android version just to be safe
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			if( sharedPreferences.getBoolean("preference_using_saf", false) ) {
				return true;
			}
        }
        return false;
    }

    // only valid if !isUsingSAF()
	private String getSaveLocation() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String folder_name = sharedPreferences.getString("preference_save_location", "ARBeauty");
		return folder_name;
    }

	private File getBaseFolder() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM); //+ File.separator + getSaveLocation()
	}

    public File getImageFolder(String folder_name) {
		File file = null;
		if( folder_name.length() > 0 && folder_name.lastIndexOf('/') == folder_name.length()-1 ) {
			// ignore final '/' character
			folder_name = folder_name.substring(0, folder_name.length()-1);
		}
		//if( folder_name.contains("/") ) {
		if( folder_name.startsWith("/") ) {
			file = new File(folder_name);
		}
		else {
	        file = new File(getBaseFolder(), folder_name);
		}
        return file;
    }

    // only valid if isUsingSAF()
    Uri getTreeUriSAF() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Uri treeUri = Uri.parse(sharedPreferences.getString("preference_save_location_saf", ""));
		return treeUri;
    }

    // only valid if isUsingSAF()
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	File getFileFromDocumentIdSAF(String id) {
	    File file = null;
        String [] split = id.split(":");
        if( split.length >= 2 ) {
            String type = split[0];
		    String path = split[1];
		    File [] storagePoints = new File("/storage").listFiles();

            if( "primary".equalsIgnoreCase(type) ) {
    			final File externalStorage = Environment.getExternalStorageDirectory();
    			file = new File(externalStorage, path);
            }
	        for(int i=0;i<storagePoints.length && file==null;i++) {
	            File externalFile = new File(storagePoints[i], path);
	            if( externalFile.exists() ) {
	            	file = externalFile;
	            }
	        }
		}
		return file;
	}

    // valid if whether or not isUsingSAF()
    // but note that if isUsingSAF(), this may return null - it can't be assumed that there is a File corresponding to the SAF Uri
	public File getImageFolder() {
		File file = null;

    		String folder_name = getSaveLocation();
    		file = getImageFolder(folder_name);

    	return file;
    }

	// only valid if isUsingSAF()
	// This function should only be used as a last resort - we shouldn't generally assume that a Uri represents an actual File, and instead.
	// However this is needed for a workaround to the fact that deleting a document file doesn't remove it from MediaStore.
	// See:
	// http://stackoverflow.com/questions/21605493/storage-access-framework-does-not-update-mediascanner-mtp
	// http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework/
    // only valid if isUsingSAF()
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public File getFileFromDocumentUriSAF(Uri uri) {

	    File file = null;
		if( "com.android.externalstorage.documents".equals(uri.getAuthority()) ) {
            final String id = DocumentsContract.getDocumentId(uri);

    		file = getFileFromDocumentIdSAF(id);
		}
		return file;
	}

	private String createMediaFilename(int type, int count) {
        String index = "";
        if( count > 0 ) {
            index = "_" + count; // try to find a unique filename
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String mediaFilename = null;
        if( type == MEDIA_TYPE_IMAGE ) {
    		String prefix = sharedPreferences.getString("preference_save_photo_prefix", "IMG_");
    		mediaFilename = prefix + timeStamp + index + ".jpg";
        }
        else if( type == MEDIA_TYPE_VIDEO ) {
    		String prefix = sharedPreferences.getString("preference_save_video_prefix", "VID_");
    		mediaFilename = prefix + timeStamp + index + ".mp4";
        }
        else {
        	throw new RuntimeException();
        }
        return mediaFilename;
    }
    
    // only valid if !isUsingSAF()
    @SuppressLint("SimpleDateFormat")
	public File createOutputMediaFile(int type) throws IOException {
    	File mediaStorageDir = getImageFolder();

        // Create the storage directory if it does not exist
        if( !mediaStorageDir.exists() ) {
			if (!mediaStorageDir.mkdirs()) {
				if (showlog)
					Log.e(TAG, "failed to create directory");
				throw new IOException();
			}
			broadcastFile(mediaStorageDir, false, false);
		}

        // Create a media file name
        File mediaFile = null;
        for(int count=0;count<100;count++) {
        	String mediaFilename = createMediaFilename(type, count);
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mediaFilename);
            if( !mediaFile.exists() ) {
            	break;
            }
        }

		if( mediaFile == null )
			throw new IOException();

        return mediaFile;
    }

    // only valid if isUsingSAF()
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public Uri createOutputMediaFileSAF(int type) throws IOException {
    	try {
	    	Uri treeUri = getTreeUriSAF();

	        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));

		    String mimeType = "";
	        if( type == MEDIA_TYPE_IMAGE ) {
	        	mimeType = "image/jpeg";
	        }
	        else if( type == MEDIA_TYPE_VIDEO ) {
	        	mimeType = "video/mp4";
	        }
	        else {
	        	// throw exception as this is a programming error
	    		if( showlog )
	    			Log.e(TAG, "unknown type: " + type);
	        	throw new RuntimeException();
	        }
	        // note that DocumentsContract.createDocument will automatically append to the filename if it already exists
	        String mediaFilename = createMediaFilename(type, 0);
		    Uri fileUri = DocumentsContract.createDocument(context.getContentResolver(), docUri, mimeType, mediaFilename);
			if( fileUri == null )
				throw new IOException();
	    	return fileUri;
    	}
    	catch(IllegalArgumentException e) {
    		// DocumentsContract.getTreeDocumentId throws this if URI is invalid
		    if( showlog )
		    	Log.e(TAG, "createOutputMediaFileSAF failed");
		    e.printStackTrace();
		    throw new IOException();
    	}
    }

    public class Media {
    	public long id;
    	public boolean video;
    	public Uri uri;
    	long date;
    	public int orientation;

    	Media(long id, boolean video, Uri uri, long date, int orientation) {
    		this.id = id;
    		this.video = video;
    		this.uri = uri;
    		this.date = date;
    		this.orientation = orientation;
    	}
    }
    
    private Media getLatestMedia(boolean video) {
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// needed for Android 6, in case users deny storage permission, otherwise we get java.lang.SecurityException from ContentResolver.query()
			// see https://developer.android.com/training/permissions/requesting.html
			// currently we don't bother requesting the permission, as still using targetSdkVersion 22
			if (showlog)
				Log.e(TAG, "don't have READ_EXTERNAL_STORAGE permission");
			return null;
		}

		Media media = null;
		Uri baseUri = video ? Video.Media.EXTERNAL_CONTENT_URI : Images.Media.EXTERNAL_CONTENT_URI;
		//Uri baseUri = Uri.fromFile(getImageFolder());//
		//Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();

		//Log.i("ishroid", baseUri.toString());

		Uri query = baseUri;
		final int column_id_c = 0;
		final int column_date_taken_c = 1;
		final int column_data_c = 2;
		final int column_orientation_c = 3;


		String [] projection = video ? new String[] {VideoColumns._ID, VideoColumns.DATE_TAKEN, VideoColumns.DATA} : new String[] {ImageColumns._ID, ImageColumns.DATE_TAKEN, ImageColumns.DATA, ImageColumns.ORIENTATION};
		String selection =  video ? "" : ImageColumns.MIME_TYPE + "='image/jpeg'";
		String order = video ? VideoColumns.DATE_TAKEN + " DESC"  : ImageColumns.DATE_TAKEN + " DESC";
		Cursor cursor = null;

		try {
			//cursor = context.getContentResolver().query(query, projection, selection, null, order);
			cursor = context.getContentResolver().query(query, null, null, null, order);
			if (cursor != null && cursor.moveToFirst()) {

				// now sorted in order of date - scan to most recent one in the Open Camera save folder
				boolean found = false;
				File save_folder = isUsingSAF() ? null : getImageFolder();
				String save_folder_string = isUsingSAF() ? null : save_folder.getAbsolutePath() + File.separator;
				do {
					//String path = cursor.getString(column_data_c);
					String path = cursor.getString(1);
					Log.d("Names",""+path);
					// path may be null on Android 4.4!: http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
					// and if isUsingSAF(), it's not clear how we can get the real path, or otherwise tell if an item is a subset of the SAF treeUri
					if (isUsingSAF() || (path != null && path.contains(save_folder_string))) {
						// we filter files with dates in future, in case there exists an image in the folder with incorrect datestamp set to the future
						// we allow up to 2 days in future, to avoid risk of issues to do with timezone etc
						//Log.d("Names",""+path);
						long date = cursor.getLong(column_date_taken_c);
						long current_time = System.currentTimeMillis();
						if (date > current_time + 172800000) {
						} else {
							found = true;
							break;
						}
					}
				} while (cursor.moveToNext());

				if (found) {
					//cursor.moveToFirst();
					long id = cursor.getLong(column_id_c);
					long date = cursor.getLong(column_date_taken_c);
					int orientation = video ? 0 : cursor.getInt(column_orientation_c);
					Uri uri = ContentUris.withAppendedId(baseUri, id);
					media = new Media(id, video, uri, date, orientation);
				}

			}
		}
		catch(SQLiteException e) {
			// had this reported on Google Play from getContentResolver().query() call
			if( showlog )
				Log.e(TAG, "SQLiteException trying to find latest media");
			e.printStackTrace();
		}
		finally {
			if( cursor != null ) {
				cursor.close();
			}
		}
		return media;
    }
    
    public Media getLatestMedia() {
		Media image_media = getLatestMedia(false);
		Media video_media = getLatestMedia(true);
		Media media = null;
//		if (video_media != null ) {
//			media = video_media;
//		}
		if (image_media != null && video_media == null) {
			media = image_media;

		} else if (image_media == null && video_media != null) {
			media = video_media;
		} else if (image_media != null && video_media != null) {
			if (image_media.date >= video_media.date) {
				media = image_media;
			} else {
				media = video_media;
			}
		}
		return media;
	}
}
