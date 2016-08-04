package com.next.myforismatic.ui.authorquoteslist;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.next.myforismatic.R;
import com.next.myforismatic.models.Quote;
import com.next.myforismatic.providers.QuoteContentProvider;
import com.next.myforismatic.ui.base.BaseFragment;
import com.next.myforismatic.ui.common.QuoteListAdapter;
import com.next.myforismatic.utils.CursorParse;

import java.util.List;

/**
 * Created by maslparu on 06.07.2016.
 */
public class AuthorQuoteListFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    private QuoteListAdapter adapter;
    private String author;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_author_quote_list, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.fragment_author_quote_list_rv);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new QuoteListAdapter();
        adapter.setAuthorQuotes(true);
        recyclerView.setAdapter(adapter);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            author = bundle.getString(QuoteContentProvider.QUOTE_AUTHOR);
            getActivity().setTitle(author);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(R.id.quote_cursor_loader, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == R.id.quote_cursor_loader) {
            return new MyCursorLoader(getContext(), QuoteContentProvider.QUOTE_CONTENT_URI, author);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Quote> quotes = CursorParse.parseQuotes(data);
        setQuotes(quotes);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setQuotes(@NonNull List<Quote> quotes) {
        adapter.setQuotes(quotes);
    }


    private static class MyCursorLoader extends CursorLoader {

        private Uri uri;
        private String author;

        public MyCursorLoader(Context context, Uri uri, String author) {
            super(context);
            this.uri = uri;
            this.author = author.replaceAll(" \u00a9", "");
        }

        @Override
        public Cursor loadInBackground() {
            return getContext().getContentResolver().query(
                    uri, null, QuoteContentProvider.QUOTE_AUTHOR, new String[] { author },
                    QuoteContentProvider.QUOTE_ID
            );
        }

    }
}