/*
 *  * Copyright © Wynntils - 2018.
 */

package cf.wynntils.modules.music.instances;

import cf.wynntils.modules.music.configs.MusicConfig;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class MusicPlayer {

    Thread musicPlayer;
    boolean active = false;

    float currentVolume = 1;
    File currentMusic;
    File nextMusic = null;
    AdvancedPlayer currentPlayer;

    public void play(File f) {
        if(currentMusic != null && currentMusic.getName().equalsIgnoreCase(f.getName())) return;

        nextMusic = f;
        setupController();
    }

    public void stop() {
        if(!active) return;

        currentPlayer.stop();
        active = false;
        currentMusic = null;
        nextMusic = null;
    }

    private void checkForTheEnd() {
        if(!active) return;

        nextMusic = currentMusic;
        currentMusic = null;
    }

    public void setVolume(float volume) {
        if(!active || currentPlayer == null) return;
        if(currentPlayer != null && currentPlayer.getAudioDevice() == null) return;

        if(currentPlayer.getAudioDevice() instanceof JavaSoundAudioDevice) {
            JavaSoundAudioDevice dv = (JavaSoundAudioDevice) currentPlayer.getAudioDevice();
            dv.setLineGain(volume);
            currentVolume = volume;
        }
    }

    public float getCurrentVolume() {
        return currentVolume;
    }

    private long lastSetup = 0;
    public void setupController() {
        if(System.currentTimeMillis() - lastSetup <= 150) return;

        active = true;
        if(nextMusic != null) {
            if(currentMusic == null) {
                currentMusic = nextMusic;
                nextMusic = null;
                startReproduction();
            }else{
                if(currentVolume <= -20) {
                    currentMusic = nextMusic;
                    nextMusic = null;
                    startReproduction();
                }else{
                    setVolume(getCurrentVolume() - 0.2f);
                }
            }
        }else{
            if(getCurrentVolume() > MusicConfig.INSTANCE.baseVolume) {
                if(getCurrentVolume() - 0.2f < MusicConfig.INSTANCE.baseVolume) {
                    setVolume(MusicConfig.INSTANCE.baseVolume);
                }else{ setVolume(getCurrentVolume() - 0.2f); }
            }else if(getCurrentVolume() < MusicConfig.INSTANCE.baseVolume) {
                if(getCurrentVolume() + 0.2f > MusicConfig.INSTANCE.baseVolume) {
                    setVolume(MusicConfig.INSTANCE.baseVolume);
                }else{ setVolume(getCurrentVolume() + 0.2f); }
            }
        }
    }

    private void startReproduction() {
        if(currentPlayer != null) {
            currentPlayer.stop();
            if(musicPlayer != null && musicPlayer.isAlive()) musicPlayer.stop();
        }

        musicPlayer = new Thread(() -> {
            try{
                FileInputStream fis = new FileInputStream(currentMusic);
                BufferedInputStream bis = new BufferedInputStream(fis);
                currentPlayer = new AdvancedPlayer(bis);
                currentPlayer.setPlayBackListener(new PlaybackListener() {
                    public void playbackFinished(PlaybackEvent var1) { checkForTheEnd(); }
                });

                currentPlayer.play();
                fis.close();
                bis.close();
            }catch (Exception ex) { ex.printStackTrace(); }
        });
        musicPlayer.setName("Wynntils - Music Reproducer"); musicPlayer.start();
    }

}
