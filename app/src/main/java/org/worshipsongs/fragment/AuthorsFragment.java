package org.worshipsongs.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.worshipsongs.CommonConstants;
import org.worshipsongs.R;
import org.worshipsongs.activity.SongListActivity;
import org.worshipsongs.adapter.TitleAdapter;
import org.worshipsongs.domain.Author;
import org.worshipsongs.domain.Type;
import org.worshipsongs.listener.SongContentViewListener;
import org.worshipsongs.registry.ITabFragment;
import org.worshipsongs.service.AuthorService;
import org.worshipsongs.service.UserPreferenceSettingService;
import org.worshipsongs.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @Author : Seenivasan,Madasamy
 * @Version : 1.0
 */
public class AuthorsFragment extends Fragment implements TitleAdapter.TitleAdapterListener<Author>, ITabFragment
{
    private static final String STATE_KEY = "listViewState";
    private Parcelable state;
    private AuthorService authorService;
    private List<Author> authorList = new ArrayList<>();
    private ListView authorListView;
    private TitleAdapter<Author> titleAdapter;
    private UserPreferenceSettingService userPreferenceSettingService = new UserPreferenceSettingService();

    public static AuthorsFragment newInstance()
    {
        return new AuthorsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            state = savedInstanceState.getParcelable(STATE_KEY);
        }
        setHasOptionsMenu(true);
        initSetUp();
    }

    private void initSetUp()
    {
        authorService = new AuthorService(getContext());
        for (Author author : authorService.findAll()) {
            if (!author.getName().toLowerCase().contains("unknown") && author.getName() != null) {
                authorList.add(author);
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = (View) inflater.inflate(R.layout.songs_layout, container, false);
        setListView(view);
        return view;
    }

    private void setListView(View view)
    {
        authorListView = (ListView) view.findViewById(R.id.song_list_view);
        titleAdapter = new TitleAdapter<Author>((AppCompatActivity) getActivity(), R.layout.songs_layout);
        titleAdapter.setTitleAdapterListener(this);
        titleAdapter.addObjects(authorService.getAuthors("", authorList));
        authorListView.setAdapter(titleAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.action_bar_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getString(R.string.action_search));
        ImageView image = (ImageView) searchView.findViewById(R.id.search_close_btn);
        Drawable drawable = image.getDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                titleAdapter.addObjects(authorService.getAuthors(query, authorList));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                titleAdapter.addObjects(authorService.getAuthors(newText, authorList));
                return true;

            }
        });
        menu.getItem(0).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (this.isAdded()) {
            outState.putParcelable(STATE_KEY, authorListView.onSaveInstanceState());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (state != null) {
            authorListView.onRestoreInstanceState(state);
        } else {
            titleAdapter.addObjects(authorService.getAuthors("", authorList));
        }
    }

    @Override
    public void onPause()
    {
        state = authorListView.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            CommonUtils.hideKeyboard(getActivity());
        }
    }

    @Override
    public void setViews(Map<String, Object> objects, Author author)
    {
        TextView titleTextView = (TextView) objects.get(CommonConstants.TITLE_KEY);
        titleTextView.setText(getAuthorName(author));
        titleTextView.setOnClickListener(textViewOnClickListener(author));
        TextView countTextView = (TextView) objects.get(CommonConstants.COUNT_KEY);
        String count = String.valueOf(author.getNoOfSongs());
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) countTextView.getLayoutParams();
        if (count.toCharArray().length == 3) {
            params.width = 130;
            countTextView.setLayoutParams(params);
        } else if (count.toCharArray().length == 2) {
            params.width = 100;
            countTextView.setLayoutParams(params);
        } else if (count.toCharArray().length == 1) {
            params.width = 70;
            countTextView.setLayoutParams(params);
        } else {
            params.width = 200;
            countTextView.setLayoutParams(params);
        }
        countTextView.setText(count);
        countTextView.setVisibility(StringUtils.isNotBlank(countTextView.getText()) ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private View.OnClickListener textViewOnClickListener(final Author author)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getContext(), SongListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CommonConstants.TYPE, Type.AUTHOR.name());
                intent.putExtra(CommonConstants.TITLE_KEY, getAuthorName(author));
                intent.putExtra(CommonConstants.ID, author.getId());
                startActivity(intent);
            }
        };
    }

    private String getAuthorName(Author author)
    {
        return userPreferenceSettingService.isTamil() ? author.getTamilName() : author.getDefaultName();
    }


    @Override
    public int defaultSortOrder()
    {
        return 1;
    }

    @Override
    public String getTitle()
    {
        return "artists";
    }

    @Override
    public boolean checked()
    {
        return true;
    }

    @Override
    public void setListenerAndBundle(SongContentViewListener songContentViewListener, Bundle bundle)
    {
        //DO nothing
    }


}
