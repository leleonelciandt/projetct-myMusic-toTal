package com.ciandt.summit.bootcamp2022.service;

import com.ciandt.summit.bootcamp2022.common.exception.service.MusicNotFoundException;
import com.ciandt.summit.bootcamp2022.common.exception.service.MusicNotFoundInPlaylistException;
import com.ciandt.summit.bootcamp2022.common.exception.service.MusicsAndArtistsNotFoundException;
import com.ciandt.summit.bootcamp2022.common.exception.service.PlaylistNotFoundException;
import com.ciandt.summit.bootcamp2022.entity.Artist;
import com.ciandt.summit.bootcamp2022.entity.Music;
import com.ciandt.summit.bootcamp2022.entity.Playlist;
import com.ciandt.summit.bootcamp2022.repository.MusicRepository;
import com.ciandt.summit.bootcamp2022.repository.PlaylistRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;


import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@MockitoSettings
public class PlaylistServiceTest {
    @Mock
    private MusicRepository musicRepositoryMocked;

    @Mock
    private PlaylistRepository playlistRepositoryMocked;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    @AfterEach
    void resetMocking() {
        Mockito.reset(musicRepositoryMocked, playlistRepositoryMocked);
    }

    @DisplayName(value = "Check if findPlaylistById search by id")
    @ParameterizedTest(name = "With Id {0}")
    @ValueSource(strings = {"1", "2"})
    void testIfPlaylistExistsById(String id) {
        when(playlistRepositoryMocked.findById(id)).thenReturn(Optional.of(new Playlist()));

        playlistService.findPlaylistById(id);
        verify(playlistRepositoryMocked, times(1)).findById(id);
    }

    @DisplayName(value = "Check if findPlaylistById throws exception when there is no result")
    @ParameterizedTest(name = "With Id {0}")
    @ValueSource(strings = {"notExistent"})
    void testIfPlaylistNotExistsById(String id) {
        Exception e = assertThrowsExactly(PlaylistNotFoundException.class,
                () -> playlistService.findPlaylistById(id));
        assertEquals("Playlist with Id " + id + " not found \uD83D\uDE41", e.getMessage());
    }

    @DisplayName(value = "Check if findMusicById search by id")
    @ParameterizedTest(name = "With Id {0}")
    @ValueSource(strings = {"1", "2"})
    void testIfMusicExistsById(String id) {
        when(musicRepositoryMocked.findById(id)).thenReturn(Optional.of(new Music()));

        playlistService.findMusicById(id);
        verify(musicRepositoryMocked, times(1)).findById(id);
    }

    @DisplayName(value = "Check if findMusicById throws exception when there is no result")
    @ParameterizedTest(name = "With Id {0}")
    @ValueSource(strings = {"notExistent"})
    void testIfMusicNotExistsById(String id) {
        Exception e = assertThrowsExactly(MusicNotFoundException.class,
                () -> playlistService.findMusicById(id));

        assertEquals("Music with Id " + id + " not found \uD83D\uDE41", e.getMessage());
    }

    @DisplayName(value = "Check if an exception is thrown when playlist")
    @ParameterizedTest(name = "With Id {1} doesn't have musics")
    @ValueSource(strings = {"1"})
    void testFindMusicsByPlaylistIdWithoutMusics(String playlistId) {
        when(playlistRepositoryMocked.findById(playlistId)).thenReturn(Optional.of(new Playlist()));
        when(musicRepositoryMocked.findMusicsByPlaylistId(any())).thenReturn(new HashSet<>());

        assertThrowsExactly(MusicsAndArtistsNotFoundException.class,
                () -> playlistService.findMusicsByPlaylistId(playlistId));
    }

    @Nested
    @DisplayName(value = "Testing adding/removing/finding musics in playlist")
    class testMethodAddRemoveAndFindMusicsInPlaylist {
        private Music musicAndArtistValidMusic;
        private Artist musicAndArtistValidArtist;
        private Set<Music> musics;
        private Playlist playlistEmpty;

        @BeforeEach
        void setUp() {
            musicAndArtistValidMusic = new Music("1", "valid music");
            musicAndArtistValidArtist = new Artist("1", "valid artist");
            musicAndArtistValidMusic.setArtist(musicAndArtistValidArtist);

            musics = new HashSet<>(Collections.singletonList(musicAndArtistValidMusic));

            playlistEmpty = new Playlist("1");
            playlistEmpty.setMusics(new HashSet<>());

            Mockito.reset(musicRepositoryMocked, playlistRepositoryMocked);
        }

        @DisplayName(value = "Add music to playlist")
        @ParameterizedTest(name = "With musicId {0} and playlistId {1}")
        @CsvSource(value = {"1|1"}, delimiter = '|')
        void testAddOneMusicToPlaylist(String musicId, String playlistId) {
            when(musicRepositoryMocked.findById(musicId)).thenReturn(Optional.of(musicAndArtistValidMusic));
            when(playlistRepositoryMocked.findById(playlistId)).thenReturn(Optional.of(playlistEmpty));

            Playlist playlistExpected = new Playlist("1");
            playlistExpected.setMusics(new HashSet<>(Collections.singletonList(musicAndArtistValidMusic)));

            playlistService.addMusicsToPlaylist(musics, playlistId);

            ArgumentCaptor<Playlist> playlistArgumentCaptor = ArgumentCaptor.forClass(Playlist.class);

            verify(playlistRepositoryMocked).save(playlistArgumentCaptor.capture());

            Playlist playlistCaptured = playlistArgumentCaptor.getValue();

            assertEquals(playlistExpected, playlistCaptured);
            assertEquals(playlistExpected.getMusics(), playlistCaptured.getMusics());
        }

        @DisplayName(value = "Don't add non-existing music")
        @ParameterizedTest(name = "With musicId {0} and playlistId {1}")
        @CsvSource(value = {"1|1"}, delimiter = '|')
        void testDontAddNonExistingMusicToPlaylist(String musicId, String playlistId) {
            when(musicRepositoryMocked.findById(anyString())).thenReturn(Optional.empty());
            when(playlistRepositoryMocked.findById(playlistId)).thenReturn(Optional.of(playlistEmpty));

            Exception e = assertThrowsExactly(MusicNotFoundException.class,
                    () -> playlistService.addMusicsToPlaylist(musics, playlistId));

            assertEquals("Music with Id " + musicId + " not found \uD83D\uDE41", e.getMessage());
        }

        @DisplayName(value = "Don't add multiple existent and non-existing musics")
        @ParameterizedTest(name = "to playlistId {0}")
        @ValueSource(strings = {"1"})
        void testDontAddMultipleExistingAndNonExistingMusicsToPlaylist(String playlistId) {
            when(musicRepositoryMocked.findById("1")).thenReturn(Optional.of(new Music()));
            when(musicRepositoryMocked.findById("2")).thenReturn(Optional.empty());
            when(playlistRepositoryMocked.findById(playlistId)).thenReturn(Optional.of(playlistEmpty));

            musics.add(new Music("2", "not existent"));

            Exception e = assertThrowsExactly(MusicNotFoundException.class,
                    () -> playlistService.addMusicsToPlaylist(musics, playlistId));

            assertEquals("Music with Id 2 not found \uD83D\uDE41", e.getMessage());
            verify(playlistRepositoryMocked, times(0)).save(playlistEmpty);
            assertTrue(playlistEmpty.getMusics().isEmpty());
        }

        @DisplayName(value = "Check if an exception is thrown when playlist")
        @ParameterizedTest(name = "With Id {1} doesn't exist")
        @ValueSource(strings = {"1"})
        void testDontAddMusicsWhenPlaylistNotExists(String playlistId) {
            when(playlistRepositoryMocked.findById(playlistId)).thenReturn(Optional.empty());

            Exception e = assertThrowsExactly(PlaylistNotFoundException.class,
                    () -> playlistService.addMusicsToPlaylist(musics, playlistId));

            assertEquals("Playlist with Id " + playlistId + " not found \uD83D\uDE41",
                    e.getMessage());
        }

        @DisplayName(value = "Check if findMusicsByPlaylistId returns musics")
        @Test
        void testfindMusicsByPlaylistIdSuccessful() {
            playlistEmpty.setMusics(musics);
            when(playlistRepositoryMocked.findById(any())).thenReturn(Optional.of(playlistEmpty));
            when(musicRepositoryMocked.findMusicsByPlaylistId(playlistEmpty.getId())).thenReturn(playlistEmpty.getMusics());
            assertEquals(musics,
                    playlistService.findMusicsByPlaylistId(playlistEmpty.getId()));
        }

        @DisplayName(value = "Check if findMusicInPlaylistByMusicId returns music")
        @Test
        void testFindMusicInPlaylistByMusicId() {
            playlistEmpty.setMusics(musics);
            assertEquals(musicAndArtistValidMusic,
                    playlistService.findMusicInPlaylistByMusicId(playlistEmpty, musicAndArtistValidMusic.getId()));
        }
        @DisplayName(value = "Check if an exception is thrown when playlist")
        @ParameterizedTest(name = "With Id {1} doesn't have musics")
        @ValueSource(strings = {"1"})
        void testFindMusicsInPlayListByMusicIdException(String musicId) {
            assertThrowsExactly(MusicNotFoundInPlaylistException.class,
                    () -> playlistService.findMusicInPlaylistByMusicId(playlistEmpty, musicId));
        }

        @DisplayName(value = "Don't add multiple existent and non-existing musics")
        @ParameterizedTest(name = "to playlistId {0}")
        @ValueSource(strings = {"1"})
        void testRemoveMusicById(String playlistId) {
            when(musicRepositoryMocked.findById("1")).thenReturn(Optional.of(musicAndArtistValidMusic));
            playlistEmpty.setMusics(musics);
            when(playlistRepositoryMocked.findById(playlistId)).thenReturn(Optional.of(playlistEmpty));

            playlistService.removeMusicFromPlaylistByMusicId(playlistId, musicAndArtistValidMusic.getId());

            verify(playlistRepositoryMocked, times(1)).save(playlistEmpty);
            assertTrue(playlistEmpty.getMusics().isEmpty());
        }
    }

}
