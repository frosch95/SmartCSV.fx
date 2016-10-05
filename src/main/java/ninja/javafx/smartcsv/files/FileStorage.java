package ninja.javafx.smartcsv.files;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import ninja.javafx.smartcsv.FileReader;
import ninja.javafx.smartcsv.FileWriter;

import java.io.File;
import java.io.IOException;

/**
 * This class stores files and their state
 * @author abi
 */
public class FileStorage<E> {

    private FileReader<E> reader;
    private FileWriter<E> writer;

    public FileStorage(FileReader<E> reader, FileWriter<E> writer) {
        this.reader = reader;
        this.writer = writer;
    }

    private BooleanProperty fileChanged = new SimpleBooleanProperty(true);
    private ObjectProperty<File> file = new SimpleObjectProperty<>();
    private ObjectProperty<E> content = new SimpleObjectProperty<>();

    public boolean isFileChanged() {
        return fileChanged.get();
    }

    public BooleanProperty fileChangedProperty() {
        return fileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        this.fileChanged.set(fileChanged);
    }

    public File getFile() {
        return file.get();
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public E getContent() {
        return content.get();
    }

    public ObjectProperty<E> contentProperty() {
        return content;
    }

    public void setContent(E content) {
        this.content.set(content);
    }

    public void load() throws IOException {
        reader.read(file.get());
        setContent(reader.getContent());
        setFileChanged(false);
    }

    public void save() throws IOException {
        writer.setContent(content.get());
        writer.write(file.get());
        setFileChanged(false);
    }
}
