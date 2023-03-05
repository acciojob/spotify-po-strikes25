package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newuser = new User(name, mobile);
        users.add(newuser);
        return newuser;
    }

    public Artist createArtist(String name) {
        Artist newartist = new Artist(name);
        artists.add(newartist);
        return newartist;
    }

    public Album createAlbum(String title, String artistName) {
        // To avoid DRY
        Artist newartist = null;
        for (Artist data : artists) {
            if (data.getName().equals(artistName)) {
                newartist = data;
            }
        }
        if(newartist == null)
            createArtist(artistName);
        Album newalbum = new Album(title);
        // To add a new album every time into the consolidated list
        albums.add(newalbum);
        return newalbum;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        // To avoid DRY
        Album newalbum = null;
        for (Album data : albums) {
            if (data.getTitle().equals(albumName)) {
                newalbum = data;
            }
        }
        if(newalbum == null)
            throw new RuntimeException("Album does not exist");
        Song newsong = new Song(title, length);
        // Since likes are not automatically being set
        newsong.setLikes(0);
        songs.add(newsong);

        // Inserting the song into the album
        if(albumSongMap.containsKey(newalbum)) {
            List<Song> existingsonglist = albumSongMap.get(newalbum);
            existingsonglist.add(newsong);
            albumSongMap.put(newalbum, existingsonglist);
        } else {
            List<Song> newsonglist = new ArrayList<>();
            newsonglist.add(newsong);
            albumSongMap.put(newalbum, newsonglist);
        }
        return newsong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = null;
        for(User newuser : users) {
            if(newuser.getMobile().equals(mobile)){
                user = newuser;
            }
        }
        if(user == null)
            throw new RuntimeException("User does not exist");
        else {
            Playlist newplaylist = new Playlist(title);
            playlists.add(newplaylist);

            List<Song> newsonglist = new ArrayList<>();
            for(Song songdata : songs) {
                if(songdata.getLength() == length)
                    newsonglist.add(songdata);
            }

            List<User> listenerList = new ArrayList<>();
            if(playlistListenerMap.containsKey(newplaylist)) listenerList= playlistListenerMap.get(newplaylist);
            listenerList.add(user);
            playlistListenerMap.put(newplaylist,listenerList);

            playlistSongMap.put(newplaylist,newsonglist);

            creatorPlaylistMap.put(user,newplaylist);

            List<Playlist> playList = new ArrayList<>();
            if(userPlaylistMap.containsKey(user)){
                playList = userPlaylistMap.get(user);
            }
            playList.add(newplaylist);
            userPlaylistMap.put(user,playList);

            return newplaylist;
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User newuser = null;
        for(User userdata : users){
            if(userdata.getMobile().equals(mobile))
                newuser = userdata;
        }

        if(newuser == null)
            throw new Exception("User does not exist");
        else {
            Playlist playlist = new Playlist(title);

            List<Song> newsongList = new ArrayList<>();
            for(String songTitle : songTitles){
                for(Song songdata : songs){
                    if(songdata.getTitle().equals(songTitle))
                        newsongList.add(songdata);
                }
            }
            List<User> listenerList = new ArrayList<>();
            if(playlistListenerMap.containsKey(playlist)) listenerList= playlistListenerMap.get(playlist);
            listenerList.add(newuser);
            playlistListenerMap.put(playlist,listenerList);

            playlistSongMap.put(playlist,newsongList);
            creatorPlaylistMap.put(newuser,playlist);

            List<Playlist> playlistList = new ArrayList<>();
            if(userPlaylistMap.containsKey(newuser)){
                playlistList = userPlaylistMap.get(newuser);
            }
            playlistList.add(playlist);
            userPlaylistMap.put(newuser,playlistList);

            return playlist;
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User newuser = null;
        for(User userdata : users){
            if(userdata.getMobile().equals(mobile))
                newuser = userdata;
        }

        if(newuser == null)
            throw new Exception("User does not exist");

        Playlist newplaylist = null;
        for(Playlist currPlaylist: playlists){
            if(currPlaylist.getTitle().equals(playlistTitle))
                newplaylist = currPlaylist;
        }

        if(newplaylist == null)
            throw new Exception("Playlist does not exist");

        if(creatorPlaylistMap.containsKey(newuser)){
            if(creatorPlaylistMap.get(newuser).getTitle().equals(playlistTitle))
                return newplaylist;
        }

        List<User> listenerList = new ArrayList<>();
        if(playlistListenerMap.containsKey(newplaylist)){
            listenerList = playlistListenerMap.get(newplaylist);
            for(User user1 : listenerList){
                if(user1.getMobile().equals(mobile))
                    return newplaylist;
            }
        }

        listenerList.add(newuser);
        playlistListenerMap.put(newplaylist,listenerList);
        userPlaylistMap.get(newuser).add(newplaylist);

        return newplaylist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User newuser = null;
        for(User userdata : users){
            if(userdata.getMobile().equals(mobile))
                newuser = userdata;
        }

        if(newuser == null)
            throw new Exception("User does not exist");

        Song newsong = null;
        for(Song songdata: songs){
            if(songdata.getTitle().equals(songTitle))
                newsong = songdata;
        }

        if(newsong == null)
            throw new Exception("Song does not exist");

        List<User> songuserlikes = new ArrayList<>();
        if(songLikeMap.containsKey(newsong))
            songuserlikes = songLikeMap.get(newsong);
        for(User userLike : songuserlikes){
            if(userLike.getMobile().equals(mobile))
                return newsong;
        }

        newsong.setLikes(newsong.getLikes()+1);
        songuserlikes.add(newuser);
        songLikeMap.put(newsong,songuserlikes);

        int like = -1;
        String mostpopularArtist = "";
        for(Artist artist: artists){
            if(artistAlbumMap.containsKey(artist)){
                List<Album> newalbumList= artistAlbumMap.get(artist);
                for (Album album : newalbumList){
                    if(albumSongMap.containsKey(album)){
                        List<Song> newsongList = albumSongMap.get(album);
                        for (Song song : newsongList){
                            if(song.equals(newsong)){
                                artist.setLikes(artist.getLikes()+1);
                            }
                        }
                    }
                }
            }
        }
        return newsong;
    }

    public String mostPopularArtist() {
        String mostpopularArtist = "";
        int like = -1;
        for (Artist artist: artists){
            if(like < artist.getLikes()){
                like = artist.getLikes();
                mostpopularArtist = artist.getName();
            }
        }
        return mostpopularArtist;
    }

    public String mostPopularSong() {
        String mostpopularSong = "";
        int like = -1;
        for (Song songdata: songs){
            if(like < songdata.getLikes()){
                like = songdata.getLikes();
                mostpopularSong = songdata.getTitle();
            }
        }
        return mostpopularSong;
    }
}
