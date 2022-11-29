package com.escript.data;

import com.escript.domain.Friendship;
import com.escript.exceptions.ID_NotFoundException;
import com.escript.exceptions.contextful.FriendshipDoesNotExistException;

import java.nio.file.Path;

public class FriendshipsFileRepository extends FileRepository<Long, Friendship>{
    /**
     * Constructor a Friendship repository
     *
     * @param id_generator An object that will generate a unique ID for every new element
     */
    public FriendshipsFileRepository(ID_Generator<Long> id_generator, Path filepath) {
        super(filepath, filepath, Friendship::fromCSV);
    }

    public Friendship getByMembers(long id1, long id2) {
        for (var friendship : this.contentSet){
            if(friendship.getUserID1() == id1 && friendship.getUserID2() == id2 ||
                    friendship.getUserID2() == id2 && friendship.getUserID1() == id2
            ) return friendship;
        }
        return null;
    }

    public void removeByMembers(long id1, long id2) throws FriendshipDoesNotExistException {
        Friendship friendship = getByMembers(id1, id2);
        if (friendship == null) {
            throw new FriendshipDoesNotExistException();
        }

        try {
            remove(friendship.getIdentifier());
        } catch (ID_NotFoundException e) {
            throw new FriendshipDoesNotExistException();
        }
    }

    public Iterable<Friendship> friendshipsOf(long id) {
        return stream().filter(x -> x.getUserID1() == id || x.getUserID2() == id).toList();
    }
}
