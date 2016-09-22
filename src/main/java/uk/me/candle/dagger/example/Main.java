package uk.me.candle.dagger.example;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {

    public static void main(String[] rawArgs) {

        final Args args = new Args();
        final CmdLineParser clp = new CmdLineParser(args);

        try {
            clp.parseArgument(rawArgs);
            MainComponent t = DaggerMainComponent.create();
            t.s3Lister().run(args);

        } catch (final CmdLineException e) {
            System.err.println(e.getMessage());
            clp.printSingleLineUsage(System.err);
            System.err.println();
            clp.printUsage(System.err);
        }
    }
}

@Component(modules = {S3DepsModule.class})
interface MainComponent {
    S3Lister s3Lister();
}

class Args {
    @Option(name = "--bucket", aliases = {"-b", "-B"}, metaVar = "<BUCKET>", usage = "some bucket") private String bucket;

    public String getBucket() { return bucket; }
    public boolean hasBucket() { return bucket != null; }
}

class S3Lister {
    private final AmazonS3 s3Client;
    private final Lazy<BucketPrinter> bucketPrinter;
    private final Lazy<S3ObjectPrinter> objectPrinter;

    @Inject
    public S3Lister(AmazonS3 s3Client, Lazy<BucketPrinter> bucketPrinter, Lazy<S3ObjectPrinter> objectPrinter) {
        this.s3Client = s3Client;
        this.bucketPrinter = bucketPrinter;
        this.objectPrinter = objectPrinter;
    }

    public void run(Args args) {
        if (args.hasBucket()) {
            s3Client.listObjects(args.getBucket()).getObjectSummaries()
                    .forEach(o -> objectPrinter.get().printBucket(o));
        } else {
            s3Client.listBuckets().forEach(b -> bucketPrinter.get().printBucket(b));
        }
    }
}

class S3ObjectPrinter {
    @Inject public S3ObjectPrinter() { }

    void printBucket(S3ObjectSummary o) {
        System.out.println(String.format("s3://%s/%s", o.getBucketName(), o.getKey()));
    }
}
class BucketPrinter {
    @Inject public BucketPrinter() { }

    void printBucket(Bucket b) {
        System.out.println(b.getCreationDate() + " " + b.getName());
    }
}

@Module(includes = {AwsDepsModule.class})
class S3DepsModule {
    @Provides
    AmazonS3 provideS3Client(ClientConfiguration clientConfiguration) {
        return new AmazonS3Client(clientConfiguration);
    }
}

@Module
class AwsDepsModule {
    @Provides
    ClientConfiguration provideClientConfiguration() {
        ClientConfiguration conf = new ClientConfiguration();
        String[] envVars = {"https_proxy", "http_proxy"};
        for (String var : envVars) {
            String proxy;
            if ((proxy = System.getenv(var)) != null) {
                try {
                    String p = proxy.startsWith("http") ? proxy : "http://" + proxy;
                    URL u = new URL(p);
                    conf.setProxyHost(u.getHost());
                    conf.setProxyPort(u.getPort());
                    return conf;
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException("Unable to understand the environment variable " + var + " with the value of " + proxy, ex);
                }
            }
        }
        return conf;
    }
}
