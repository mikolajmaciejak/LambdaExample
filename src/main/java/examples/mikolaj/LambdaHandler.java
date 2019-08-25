package examples.mikolaj;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.s3.AmazonS3;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context; 

public class LambdaHandler implements RequestHandler<Request, Response> {

	@Override
	public Response handleRequest(Request input, Context context) {
		System.out.println("Starting...");
		String urlString = input.getUrl();
		Response response = new Response();
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			JSONObject jsonObject = new JSONObject(new JSONTokener(is));
			String dt = jsonObject.getString("currentDateTime");
			String secret = "hello";
			//String secret = LambdaHandler.getSecret();
			AWSCredentials credentials = new BasicAWSCredentials(
					  input.getAccessKey(), 
					  input.getSecretKey()
					);
			AmazonS3 s3client = AmazonS3ClientBuilder
					  .standard()
					  .withCredentials(new AWSStaticCredentialsProvider(credentials))
					  .withRegion(Regions.EU_CENTRAL_1)
					  .build();
			/*		  
			AmazonS3 s3client = AmazonS3ClientBuilder
					  .standard()
					  .withRegion(Regions.EU_CENTRAL_1)
					  .build();
			*/
			String bucketName = input.getBucketName();
			s3client.putObject(bucketName, dt, secret);
			response.setResult(dt+" "+secret);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error!");
			response.setResult("fail");
		}
		return response;
	}
	
	public static String getSecret() {

	    String secretName = "my_secret";
	    String region = "eu-central-1";

	    // Create a Secrets Manager client
	    AWSSecretsManager client  = AWSSecretsManagerClientBuilder.standard()
	                                    .withRegion(region)
	                                    .build();
	    
	    // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
	    // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
	    // We rethrow the exception by default.
	    
	    String secret;
	    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
	                    .withSecretId(secretName);
	    GetSecretValueResult getSecretValueResult = null;

	    try {
	        getSecretValueResult = client.getSecretValue(getSecretValueRequest);
	    } catch (DecryptionFailureException e) {
	        // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InternalServiceErrorException e) {
	        // An error occurred on the server side.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InvalidParameterException e) {
	        // You provided an invalid value for a parameter.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (InvalidRequestException e) {
	        // You provided a parameter value that is not valid for the current state of the resource.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    } catch (ResourceNotFoundException e) {
	        // We can't find the resource that you asked for.
	        // Deal with the exception here, and/or rethrow at your discretion.
	        throw e;
	    }

	    // Decrypts secret using the associated KMS CMK.
	    // Depending on whether the secret is a string or binary, one of these fields will be populated.
	    if (getSecretValueResult.getSecretString() != null) {
	        secret = getSecretValueResult.getSecretString();
	    }
	    else {
	        secret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
	    }
	    return secret;
	}

}
