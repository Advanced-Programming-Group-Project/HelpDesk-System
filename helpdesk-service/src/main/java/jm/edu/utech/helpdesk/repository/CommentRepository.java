package jm.edu.utech.helpdesk.repository;
//created by rickayla mcneil 2111687
import java.util.List;

import jm.edu.utech.helpdesk.model.Comment;

// comments table
// member 3 input: implement with JPA + @Repository
public interface CommentRepository {

    public abstract Comment save(Comment comment);

    // oldest first
    public abstract List<Comment> findByTicketId(Long ticketId);
}
