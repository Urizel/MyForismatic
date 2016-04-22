package com.next.myforismatic.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.next.myforismatic.R;
import com.next.myforismatic.adapters.QuoteListAdapter;
import com.next.myforismatic.common.CursorParse;
import com.next.myforismatic.models.Quote;
import com.next.myforismatic.providers.QuoteContentProvider;
import com.next.myforismatic.services.ForismaticIntentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * @author Konstantin Abramov on 20.03.16.
 */
public class QuoteListFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = "myLogs";
    private final int QUOTES_SIZE_FIRST_RUN = 10;
    private final int QUOTES_SIZE = 10;

    private RecyclerView recyclerView;
    private QuoteListAdapter adapter;

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("message").equals("finish")) {
                restartLoader();
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageReceiver);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quote_list, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.fragment_quote_list_rv);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new QuoteListAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getQuotes();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.load_more_item) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    messageReceiver,
                    new IntentFilter("endDownload")
            );
            getQuotesFromInternetService();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(R.id.quote_cursor_loader, null, this);
    }

    private void getQuotes() {
        getActivity().getSupportLoaderManager().initLoader(R.id.quote_cursor_loader, null, this).forceLoad();
    }

    private void getQuotesFromInternet() {
        new MyTask().execute();
    }

    private void getQuotesFromInternetService() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, ForismaticIntentService.class).putExtra("size", QUOTES_SIZE);
        activity.startService(intent);
    }

    public Call<Quote> getQuote(int key) {
        return getForismaticService().getQuote("getQuote", "json", "ru", key);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == R.id.quote_cursor_loader) {
            return new MyCursorLoader(getContext(), QuoteContentProvider.QUOTE_CONTENT_URI);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<Quote> list = CursorParse.parseQuotes(data);

        if (list.size() == 0) {
            getQuotesFromInternet();
        } else {
            adapter.setQuotes(list);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private class MyTask extends AsyncTask<Void, Void, List<Quote>> {

        @Override
        protected List<Quote> doInBackground(Void... params) {
            List<Quote> quotes = getQuotes();

            for (Quote quote : quotes) {
                ContentValues contentValues = quote.getContentValues();

                getContext().getContentResolver()
                        .insert(QuoteContentProvider.QUOTE_CONTENT_URI, contentValues);
            }
            return quotes;
        }

        @NonNull
        private List<Quote> getQuotes() {
            int size = QUOTES_SIZE_FIRST_RUN;
            List<Quote> quotes = new ArrayList<>(size);
            try {
                for (int i = 0; i < size; i++) {
                    Call<Quote> callQ = getQuote(i);
                    Quote quote = callQ.execute().body();
                    quotes.add(quote);
                }
            } catch (IOException ignored) {
                //not supported
            }
            return quotes;
        }

        @Override
        protected void onPostExecute(List<Quote> quotes) {
            adapter.setQuotes(quotes);
        }

    }

    private static class MyCursorLoader extends CursorLoader {

        private Uri uri;

        public MyCursorLoader(Context context, Uri uri) {
            super(context);
            this.uri = uri;
        }

        @Override
        public Cursor loadInBackground() {
            return getContext().getContentResolver().query(uri, null, null, null, null);
        }

    }

}
