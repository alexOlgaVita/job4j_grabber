package grabber;

import grabber.model.Post;

import java.io.IOException;
import java.util.List;

public interface Parse {
    List<Post> list(String link) throws IOException;
}
