package me.dmiranda.httpserver.managers;

import me.dmiranda.httpserver.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

/**
 * Created by dmiranda on 8/2/2014.
 */
public class ResourceManager implements Runnable {

    private HashMap<String, ReadOnlyResource> files = new HashMap<String, ReadOnlyResource>();

    private Path root;
    private WatchService watcher;
    private Thread worker;

    public ResourceManager(String directory) throws UnsupportedOperationException, IOException {

        root = Paths.get(directory);
        if(root == null){
            throw new UnsupportedOperationException("Invalid directory /" + root);
        }

        watcher = root.getFileName().getFileSystem().newWatchService();
        root.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        worker = new Thread(this);
        worker.start();

        Log.info(getClass().getSimpleName(), "File watcher service started on /" + root.toString());
    }

    public boolean exists(String file){
        return files.containsKey(file.toLowerCase());
    }

    public ReadOnlyResource getFile(String file){
        return files.get(file.toLowerCase());
    }

    public void clearFiles(){
        files.clear();
    }

    public void loadFiles() throws IOException {

        Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                loadFile(file);
                return super.visitFile(file, attrs);
            }
        });

    }

    public void loadFile(Path file) {

        try {

            RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());

            channel.read(buffer);
            buffer.rewind();
            buffer.flip();

            files.put(file.toString().replace("\\", "/").toLowerCase(), new ReadOnlyResource(buffer));

            Log.info("Loaded File", file.toString().replace("\\", "/").toLowerCase() + ", " + channel.size());

            channel.close();
            raf.close();

        }catch(IOException e){
            Log.error(getClass().getSimpleName() + " " + e.getClass().toString(), e);
        }
    }

    protected void onFileModify(Path path){
        loadFile(root.resolve(path));
    }

    protected void onFileDelete(Path path){
        files.remove(root.resolve(path));
    }

    protected void onFileCreate(Path path){
        loadFile(root.resolve(path));
    }

    @Override
    public void run() {

        try {

            WatchKey key = watcher.take();
            while(true){

                for(WatchEvent event : key.pollEvents()){

                    if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY){
                        onFileModify(Paths.get(event.context().toString()));
                    }
                    else if(event.kind() == StandardWatchEventKinds.ENTRY_CREATE){
                        onFileCreate(Paths.get(event.context().toString()));
                    }
                    else if(event.kind() == StandardWatchEventKinds.ENTRY_DELETE){
                        onFileDelete(Paths.get(event.context().toString()));
                    }

                }
                key.reset();
            }


        } catch (InterruptedException e) {
            Log.error("FileWatcher", e);
        }

    }
}
