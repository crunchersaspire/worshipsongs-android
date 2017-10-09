//package org.worshipsongs.service;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import android.support.annotation.NonNull;
//import android.widget.ListView;
//
//import org.apache.commons.lang3.StringUtils;
//import org.worshipsongs.CommonConstants;
//import org.worshipsongs.service.SongService;
//import org.worshipsongs.domain.ServiceSong;
//import org.worshipsongs.domain.Song;
//import org.worshipsongs.domain.Type;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * Author : Madasamy
// * Version : 3.x
// */
//
//public class SongService implements ISongService
//{
//    private SongService songDao;
//    private SharedPreferences sharedPreferences;
//    private UserPreferenceSettingService userPreferenceSettingService;
//
////    public SongService(Context context)
////    {
////        songDao = new SongService(context);
////        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
////        userPreferenceSettingService = new UserPreferenceSettingService();
////    }
////
////    @Override
////    public void copyDatabase(String databasePath, boolean dropDatabase) throws IOException
////    {
////        songDao.copyDatabase(databasePath, dropDatabase);
////    }
////
////    @Override
////    public void open()
////    {
////        songDao.open();
////    }
////
////    @Override
////    public List<Song> findAll()
////    {
////        return songDao.findAll();
////    }
//
//    @Override
//    public List<Song> filterSongs(String type, String query, List<Song> songs)
//    {
//        Set<Song> filteredSongSet = new HashSet<>();
//        if (StringUtils.isBlank(query)) {
//            filteredSongSet.addAll(songs);
//        } else {
//            for (Song song : songs) {
//                if (isSearchBySongBookNumber(type, query)) {
//                    if (getSongBookNumber(query) == song.getSongBookNumber()) {
//                        filteredSongSet.add(song);
//                    }
//                } else if (sharedPreferences.getBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, true)) {
//                    if (getTitles(song.getSearchTitle()).toString().toLowerCase().contains(query.toLowerCase())) {
//                        filteredSongSet.add(song);
//                    }
//                    if (song.getComments() != null && song.getComments().toLowerCase().contains(query.toLowerCase())) {
//                        filteredSongSet.add(song);
//                    }
//                } else {
//                    if (song.getSearchLyrics().toLowerCase().contains(query.toLowerCase())) {
//                        filteredSongSet.add(song);
//                    }
//                    if (song.getComments() != null && song.getComments().toLowerCase().contains(query.toLowerCase())) {
//                        filteredSongSet.add(song);
//                    }
//                }
//            }
//        }
//        return getSortedSongs(type, filteredSongSet);
//    }
//
//    boolean isSearchBySongBookNumber(String type, String query)
//    {
//        int songBookNumber = getSongBookNumber(query);
//        return Type.SONG_BOOK.name().equalsIgnoreCase(type) && songBookNumber >= 0 && sharedPreferences.getBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, true);
//    }
//
//    int getSongBookNumber(String query)
//    {
//        try {
//            return Integer.parseInt(query);
//        } catch (Exception ex) {
//            return -1;
//        }
//    }
//
//    @Override
//    public List<ServiceSong> filteredServiceSongs(String query, List<ServiceSong> serviceSongs)
//    {
//        List<ServiceSong> filteredServiceSongs = new ArrayList<>();
//        if (StringUtils.isBlank(query)) {
//            filteredServiceSongs.addAll(serviceSongs);
//        } else if (serviceSongs != null && !serviceSongs.isEmpty()) {
//            for (ServiceSong serviceSong : serviceSongs) {
//                if (getSearchTitles(serviceSong).toString().toLowerCase().contains(query.toLowerCase())) {
//                    filteredServiceSongs.add(serviceSong);
//                }
//                if (serviceSong.getSong() != null && serviceSong.getSong().getComments() != null &&
//                        serviceSong.getSong().getComments().toLowerCase().contains(query.toLowerCase())) {
//                    filteredServiceSongs.add(serviceSong);
//                }
//            }
//        }
//        return filteredServiceSongs;
//    }
//
////    List<String> getSearchTitles(ServiceSong serviceSong)
////    {
////        List<String> searchTitles = new ArrayList<>();
////        if (serviceSong != null && serviceSong.getSong() != null && StringUtils.isNotBlank(serviceSong.getSong().getSearchTitle())) {
////            searchTitles.addAll(getTitles(serviceSong.getSong().getSearchTitle()));
////        }
////        return searchTitles;
////    }
////
////    List<Song> getSortedSongs(String type, Set<Song> filteredSongSet)
////    {
////        if (Type.SONG_BOOK.name().equalsIgnoreCase(type)) {
////            List<Song> songs = new ArrayList<>(filteredSongSet);
////            Collections.sort(songs, Song.SONG_BOOK_NUMBER_ASC);
////            return songs;
////        } else {
////            return getSortedSongs(filteredSongSet);
////        }
////    }
//
////    @NonNull
////    private List<Song> getSortedSongs(Set<Song> filteredSongSet)
////    {
////        List<Song> tamilSongs = new ArrayList<>();
////        List<Song> englishSongs = new ArrayList<>();
////        for (Song song : filteredSongSet) {
////            if (StringUtils.isNotBlank(song.getTamilTitle())) {
////                tamilSongs.add(song);
////            } else {
////                englishSongs.add(song);
////            }
////        }
////        Collections.sort(tamilSongs, new SongComparator());
////        Collections.sort(englishSongs, new SongComparator());
////        List<Song> sortedSongs = new ArrayList<>();
////        sortedSongs.addAll(tamilSongs);
////        sortedSongs.addAll(englishSongs);
////        return sortedSongs;
////    }
//
////    @Override
////    public List<Song> findByAuthorId(int id)
////    {
////        return songDao.findByAuthorId(id);
////    }
////
////    @Override
////    public List<Song> findByTopicId(int id)
////    {
////        return songDao.findByTopicId(id);
////    }
////
////    @Override
////    public List<Song> findBySongBookId(int id)
////    {
////        return songDao.findBySongBookId(id);
////    }
////
////    @Override
////    public Song findContentsByTitle(String title)
////    {
////        return songDao.findContentsByTitle(title);
////    }
//
////    List<String> getTitles(String searchTitle)
////    {
////        return Arrays.asList(searchTitle.split("@"));
////    }
//
////    private class SongComparator implements Comparator<Song>
////    {
////        @Override
////        public int compare(Song song1, Song song2)
////        {
////            if (userPreferenceSettingService.isTamil()) {
////                int result = nullSafeStringComparator(song1.getTamilTitle(), song2.getTamilTitle());
////                if (result != 0) {
////                    return result;
////                }
////                return nullSafeStringComparator(song1.getTitle(), song2.getTitle());
////            } else {
////                return song1.getTitle().compareTo(song2.getTitle());
////            }
////        }
////
////        private int nullSafeStringComparator(final String one, final String two)
////        {
////            if (StringUtils.isBlank(one) ^ StringUtils.isBlank(two)) {
////                return (StringUtils.isBlank(one)) ? -1 : 1;
////            }
////            if (StringUtils.isBlank(one) && StringUtils.isBlank(one)) {
////                return 0;
////            }
////            return one.toLowerCase().trim().compareTo(two.toLowerCase().trim());
////        }
////
////    }
////
////    public String getTitle(boolean isTamil, ServiceSong serviceSong)
////    {
////        try {
////            return (isTamil && StringUtils.isNotBlank(serviceSong.getSong().getTamilTitle())) ?
////                    serviceSong.getSong().getTamilTitle() : serviceSong.getTitle();
////        } catch (Exception ex) {
////            return serviceSong.getTitle();
////        }
////    }
//}
