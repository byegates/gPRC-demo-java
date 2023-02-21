package blog.client;

import com.google.protobuf.Empty;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import static blog.service.BlogService.PORT;

public class BlogClient {
    public static final String tag = "[Client]";

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", PORT)
                .usePlaintext()
                .build();

        run(channel);

        System.out.printf("%s [Shutting Down]\n", tag);
        channel.shutdown();
        System.out.printf("%s [Down]\n", tag);
    }

    private static void run(ManagedChannel channel) {
        var stub = BlogServiceGrpc.newBlockingStub(channel);

        var blogId = createBlog(stub);
        if (blogId == null) return;
        readBlog(stub, blogId);
        updateBlog(stub, blogId);
        deleteBlog(stub, blogId);
        listBlogs(stub);
    }

    private static void listBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub) {
        String tag0 = tag + " [L]";
        System.out.printf("%s [Invoked]\n", tag0);
        try {
            stub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(blog -> {
                System.out.printf(formMsg("[Success]", blog.getId(), tag0));
                System.out.printf(formMsg("[Success]", blog.getAuthor(), tag0));
                System.out.printf(formMsg("[Success]", blog.getTitle(), tag0));
                System.out.printf(formMsg("[Success]", blog.getContent(), tag0));
                System.out.println();
            });
        } catch (StatusRuntimeException e) {
            System.out.printf(formMsg("[FAIL]", "Couldn't list blogs", tag0));
            e.printStackTrace();
        }
    }

    private static void deleteBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId id) {
        String tag0 = tag + " [D]";
        System.out.printf("%s [Invoked]\n", tag0);
        try {
            stub.deleteBlog(id);
            System.out.printf(formMsg("[Success]", id.getId(), tag0));
        } catch (StatusRuntimeException e) {
            System.out.printf(formMsg("[FAIL]", "Couldn't delete blog", tag0));
            e.printStackTrace();
        }
    }

    private static void updateBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId id) {
        String tag0 = tag + " [U]";
        System.out.printf("%s [Invoked]\n", tag0);
        try {
            var blog = Blog.newBuilder()
                    .setId(id.getId())
                    .setAuthor("颜怡颜悦")
                    .setTitle("无印良品")
                    .setContent("贵，贵，贵，贵 - updated")
                    .build();

            stub.updateBlog(blog);
            System.out.printf(formMsg("[Success]", blog.getId(), tag0));
            System.out.printf(formMsg("[Success]", blog.getAuthor(), tag0));
            System.out.printf(formMsg("[Success]", blog.getTitle(), tag0));
            System.out.printf(formMsg("[Success]", blog.getContent(), tag0));
        } catch (StatusRuntimeException e) {
            System.out.printf(formMsg("[FAIL]", "Couldn't update blog", tag0));
            e.printStackTrace();
        }
    }

    private static void readBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId id) {
        String tag0 = tag + " [R]";
        System.out.printf("%s [Invoked]\n", tag0);
        try {
            var blog = stub.readBlog(id);
            System.out.printf(formMsg("[Success]", blog.getId(), tag0));
            System.out.printf(formMsg("[Success]", blog.getAuthor(), tag0));
            System.out.printf(formMsg("[Success]", blog.getTitle(), tag0));
            System.out.printf(formMsg("[Success]", blog.getContent(), tag0));
        } catch (StatusRuntimeException e) {
            System.out.printf(formMsg("[FAIL]", "Couldn't read blog", tag0));
            e.printStackTrace();
        }
    }

    private static BlogId createBlog(BlogServiceGrpc.BlogServiceBlockingStub stub) {
        String tag0 = tag + " [C]";
        System.out.printf("%s [Invoked]\n", tag0);
        try {
            BlogId resp = stub.createBlog(
                    Blog.newBuilder()
                            .setAuthor("颜怡颜悦")
                            .setTitle("无印良品")
                            .setContent("贵，贵，贵，贵")
                            .build()
            );
            System.out.printf(formMsg("[Success]", resp.getId(), tag0));
            return resp;
        } catch (StatusRuntimeException e) {
            System.out.printf(formMsg("[Failed]", "Couldn't Create Blog", tag0));
            return null;
        }
    }

    private static String formMsg(String tag1, String s, String tag) {
        return String.format("%s %s %s\n", tag, tag1, s);
    }
}
