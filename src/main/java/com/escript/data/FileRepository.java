package com.escript.data;

import com.escript.domain.Storable;
import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.ID_NotFoundException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

public class FileRepository<ID_type, E extends Storable<ID_type>> extends MemoryRepository<ID_type, E>{
    private final Scanner fileIn;
    private final Path pathOut;
    private final Function<String, E> fromCSV;

    public FileRepository(Path pathIn, Path pathOut,
                          Function<String, E> fromCSV) {
        super();
        this.fromCSV = fromCSV;
        if(Files.notExists(pathIn)) {
            try {
                Files.createFile(pathIn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            fileIn = new Scanner(pathIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.pathOut = pathOut;
    }

    public void loadFromFile() {
        while(fileIn.hasNextLine()){
            String line = fileIn.nextLine();
            try {
                super.add(fromCSV.apply(line));
            } catch (DuplicateElementException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setID_Generator(ID_Generator<ID_type> id_generator){
        this.id_generator = id_generator;
    }

    public void writeToFile() {
        try {
            Files.deleteIfExists(pathOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.createFile(pathOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintWriter fileOut = null;
        try {
            fileOut = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(pathOut.toFile()),
                            StandardCharsets.UTF_8
                    ),
                    true
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.contents.values().stream().map(E::toCSV).forEach(fileOut::println);
    }

    @Override
    public void add(E e) throws DuplicateElementException {
        super.add(e);
        writeToFile();
    }

    @Override
    public void addAll(Iterable<E> iterable) throws DuplicateElementException {
        super.addAll(iterable);
        writeToFile();
    }

    @Override
    public void removeAll() {
        super.removeAll();
        writeToFile();
    }

    @Override
    public void remove(ID_type id) throws ID_NotFoundException {
        super.remove(id);
        writeToFile();
    }
}
