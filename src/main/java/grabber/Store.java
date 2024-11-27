package grabber;

import grabber.model.Post;

import java.util.List;

public interface Store extends AutoCloseable {
    Post save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
