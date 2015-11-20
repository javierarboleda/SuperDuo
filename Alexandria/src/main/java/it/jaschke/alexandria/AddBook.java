package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.scanner.ScannerActivity;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.util.ConnectivityUtil;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private static final int SCAN_UPC_REQUEST = 2;
    private EditText mEan;
    private final int LOADER_ID = 1;
    private View mRootview;
    private final String EAN_CONTENT="eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";



    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEan !=null) {
            outState.putString(EAN_CONTENT, mEan.getText().toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if user hits back button, then resultCode != RESULT_OK
        if (resultCode == Activity.RESULT_OK && requestCode == SCAN_UPC_REQUEST) {
            String upcCode = data.getStringExtra(getString(R.string.upc_code));
            mEan.setText(upcCode);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootview = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEan = (EditText) mRootview.findViewById(R.id.ean);

        mEan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                searchAndAddBookByEan(ean);
            }
        });

        mRootview.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.

                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(intent, SCAN_UPC_REQUEST) ;


//                Context context = getActivity();
//                CharSequence text = "This button should let you scan a book for its barcode!";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();

            }
        });

        mRootview.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEan.setText("");
            }
        });

        mRootview.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEan.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEan.setText("");
            }
        });

        if(savedInstanceState!=null){
            mEan.setText(savedInstanceState.getString(EAN_CONTENT));
            mEan.setHint("");
        }

        return mRootview;
    }

    private void searchAndAddBookByEan(String ean) {

        if (!ConnectivityUtil.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), "No network connectivity detected.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
        AddBook.this.restartLoader();
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mEan.getText().length()==0){
            return null;
        }
        String eanStr= mEan.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootview.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootview.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            ((TextView) mRootview.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) mRootview.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) mRootview.findViewById(R.id.bookCover)).execute(imgUrl);
            mRootview.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootview.findViewById(R.id.categories)).setText(categories);

        mRootview.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        mRootview.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields(){
        ((TextView) mRootview.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootview.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootview.findViewById(R.id.authors)).setText("");
        ((TextView) mRootview.findViewById(R.id.categories)).setText("");
        mRootview.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mRootview.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        mRootview.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
