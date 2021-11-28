package com.raywenderlich.android.contentprovidertodo.Controller.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.raywenderlich.android.contentprovidertodo.controller.ToDoDatabaseHandler
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.ALL_ITEMS
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.AUTHORITY
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.CONTENT_PATH
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.CONTENT_URI
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.COUNT
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.MULTIPLE_RECORDS_MIME_TYPE
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.SINGLE_RECORD_MIME_TYPE
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.ToDoTable.Columns.KEY_TODO_ID
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.ToDoTable.Columns.KEY_TODO_IS_COMPLETED
import com.raywenderlich.android.contentprovidertodo.controller.provider.ToDoContract.ToDoTable.Columns.KEY_TODO_NAME
import com.raywenderlich.android.contentprovidertodo.model.ToDo

class ToDoContentProvider : ContentProvider() {
    // 1
    // This is the content provider that will
    // provide access to the database
    private lateinit var db: ToDoDatabaseHandler
    private lateinit var sUriMatcher: UriMatcher

    // 2
    // Add the URI's that can be matched on
    // this content provider
    private fun initializeUriMatching() {
        sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        sUriMatcher.addURI(AUTHORITY, CONTENT_PATH, URI_ALL_ITEMS_CODE)
        sUriMatcher.addURI(AUTHORITY, "$CONTENT_PATH/#", URI_ONE_ITEM_CODE)
        sUriMatcher.addURI(AUTHORITY, "$CONTENT_PATH/$COUNT", URI_COUNT_CODE)
    }

    // 3
    // The URI Codes
    private val URI_ALL_ITEMS_CODE = 10
    private val URI_ONE_ITEM_CODE = 20
    private val URI_COUNT_CODE = 30


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("Implement this to handle requests to delete one or more rows")
    }


    override fun getType(uri: Uri): String? = when (sUriMatcher.match(uri)) {
        URI_ALL_ITEMS_CODE -> MULTIPLE_RECORDS_MIME_TYPE
        URI_ONE_ITEM_CODE -> SINGLE_RECORD_MIME_TYPE
        else -> null
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values?.let {
            val id = db.insert(it.getAsString(KEY_TODO_NAME))
            return Uri.parse("$CONTENT_URI/$id")
        }
        return null
    }

    override fun onCreate(): Boolean {
        context?.let {
            db = ToDoDatabaseHandler(it)
            // intialize the URIs
            initializeUriMatching()
        }
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        var cursor: Cursor? = null
        when (sUriMatcher.match(uri)) {
            URI_ALL_ITEMS_CODE -> {
                cursor = db.query(ALL_ITEMS)
            }
            URI_ONE_ITEM_CODE -> {
                uri.lastPathSegment?.let {
                    cursor = db.query(it.toInt())
                }
            }
            URI_COUNT_CODE -> {
                cursor = db.count()
            }
            UriMatcher.NO_MATCH -> { /*error handling goes here*/
            }
            else -> { /*unexpected problem*/
            }
        }
        return cursor

    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        values?.let {
            val toDo = ToDo(
                it.getAsLong(KEY_TODO_ID),
                it.getAsString(KEY_TODO_NAME),
                it.getAsBoolean(KEY_TODO_IS_COMPLETED)
            )
            return db.update(toDo)
        }
        return 0

    }
}