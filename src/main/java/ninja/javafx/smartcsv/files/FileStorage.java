/*
   The MIT License (MIT)
   -----------------------------------------------------------------------------

   Copyright (c) 2015-2019 javafx.ninja <info@javafx.ninja>

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in
   all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
   THE SOFTWARE.

*/

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
