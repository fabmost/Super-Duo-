package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener{

    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private TextView empty;
    private int position = ListView.INVALID_POSITION;

    private View dim;
    private FloatingActionMenu menu1;
    private FloatingActionButton btnCode, btnScan;

    private String query ="";
    private final int LOADER_ID = 10;

    public ListOfBooks() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );


        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        menu1 = (FloatingActionMenu)rootView.findViewById(R.id.menu);
        btnCode = (FloatingActionButton)rootView.findViewById(R.id.fab_code);
        btnScan = (FloatingActionButton)rootView.findViewById(R.id.fab_scan);
        dim = rootView.findViewById(R.id.layout_dim);

        btnCode.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        dim.setOnClickListener(this);

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);
        empty = (TextView) rootView.findViewById(R.id.empty_list);

        bookList.setEmptyView(empty);

        menu1.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    dim.setVisibility(View.VISIBLE);
                } else {
                    dim.setVisibility(View.GONE);
                }
            }
        });

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    }

    public void closeMenu(){
        if(menu1.isOpened()) {
            menu1.close(true);
        }
    }

    public void searchLoader(String query){
        this.query = query;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onClick(View v) {
        if(v == btnCode) {
            menu1.close(false);
            ((Callback) getActivity()).onAddSelected();
        }else if(v == btnScan) {
            menu1.close(false);
            ((Callback) getActivity()).onScanSelected();
        }else if(v == dim)
            menu1.close(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";

        if(query.length()>0){
            empty.setText(R.string.empty_search);
            query = "%"+query+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{query,query},
                    null
            );
        }else{
            empty.setText(R.string.empty_list);
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }
}
