package blog.service;

import com.google.protobuf.Empty;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static blog.service.BlogService.tag;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    BlogServiceImpl(MongoClient client) {
        var db = client.getDatabase("blogdb");
        mongoCollection = db.getCollection("blog");
    }

    @Override
    public void createBlog(Blog req, StreamObserver<BlogId> responseObserver) {
        String tag0 = tag + " [C]";
        System.out.printf("%s [Invoked]\n\n", tag0);
        var doc = new Document("author", req.getAuthor())
                .append("title", req.getTitle())
                .append("content", req.getContent());

        InsertOneResult res;

        try {
            res = mongoCollection.insertOne(doc);
        } catch (MongoException e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(formMsg("[Error]", e.getMessage(), tag0))
                            .asRuntimeException());
            return;
        }

        if (!res.wasAcknowledged() || res.getInsertedId() == null) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(formMsg("[FAILED]", "", tag0))
                            .asRuntimeException());
            return;
        }

        var id = res.getInsertedId().asObjectId().getValue().toString();
        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(BlogId req, StreamObserver<Blog> responseObserver) {
        String tag0 = tag + " [R]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        var id = req.getId();
        if (id.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                    .withDescription(formMsg("[Error]", "BlogId can't be Empty", tag0))
                    .asRuntimeException());
            return;
        }

        var res = mongoCollection.find(eq("_id", new ObjectId(id))).first();

        if (res == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(formMsg("[Error] Blog was not found with id:", id, tag0))
                            .asRuntimeException());
            return;

        }

        responseObserver.onNext(
                Blog.newBuilder()
                        .setAuthor(res.getString("author"))
                        .setTitle(res.getString("title"))
                        .setContent(res.getString("content"))
                        .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(Blog req, StreamObserver<Empty> responseObserver) {
        String tag0 = tag + " [U]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        var id = req.getId();
        if (id.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(formMsg("[Error]", "BlogId can't be Empty", tag0))
                            .asRuntimeException());
            return;
        }

        var res = mongoCollection.findOneAndUpdate(
                eq("_id", new ObjectId(id)),
                combine(
                        set("author", req.getAuthor()),
                        set("title", req.getTitle()),
                        set("content", req.getContent())
                ));

        if (res == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(formMsg("[Error] Blog was not found with id:", id, tag0))
                            .asRuntimeException());
            return;

        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId req, StreamObserver<Empty> responseObserver) {
        String tag0 = tag + " [D]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        var id = req.getId();
        if (id.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(formMsg("[Error]", "BlogId can't be Empty", tag0))
                            .asRuntimeException());
            return;
        }

        DeleteResult res;
        try {
            res = mongoCollection.deleteOne(eq("_id", new ObjectId(id)));
        } catch (MongoException e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(formMsg("[FAIL]", "BlogId can't be Deleted", tag0))
                            .asRuntimeException());
            return;
        }

        if (!res.wasAcknowledged()) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(formMsg("[FAIL]", "BlogId can't be Deleted", tag0))
                            .asRuntimeException());
        }

        if (res.getDeletedCount() == 0) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(formMsg("[ERROR] BlogId was not found:", id, tag0))
                            .asRuntimeException());
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(Empty req, StreamObserver<Blog> responseObserver) {
        String tag0 = tag + " [L]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        for (var doc : mongoCollection.find()) {
            responseObserver.onNext(
                    Blog.newBuilder()
                            .setId(doc.getObjectId("_id").toString())
                            .setAuthor(doc.getString("author"))
                            .setTitle(doc.getString("title"))
                            .setContent(doc.getString("content"))
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    private String formMsg(String tag1, String s, String tag) {
        return String.format("%s %s %s\n", tag, tag1, s);
    }
}
