package grabber;

import grabber.model.Post;

import java.util.ArrayList;
import java.util.List;

public class MemStore implements Store {
    private final List<Post> posts = new ArrayList<>();
    private int ids = 1;

    private int indexOf(int id) {
        int result = -1;
        for (int index = 0; index < posts.size(); index++) {
            if (posts.get(index).getId() == id) {
                result = index;
                break;
            }
        }
        return result;
    }

    public Post save(Post post) {
        post.setId(ids++);
        posts.add(post);
        return post;
    }

    public List<Post> getAll() {
        return List.copyOf(posts);
    }

    public Post findById(int id) {
        int index = indexOf(id);
        return index != -1 ? posts.get(index) : null;
    }

    @Override
    public void close() throws Exception {

    }
}