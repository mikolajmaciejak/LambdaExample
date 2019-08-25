import org.junit.Test;

import examples.mikolaj.LambdaHandler;
import examples.mikolaj.Request;
import examples.mikolaj.Response;
import org.junit.Assert;

public class TestHandler {
	
	//@Test
	public void test() {
		LambdaHandler handler = new LambdaHandler();
		Request request = new Request();
		request.setUrl("http://worldclockapi.com/api/json/utc/now");
		request.setAccessKey("XXX");
		request.setSecretKey("YYY");
		Response resp = handler.handleRequest(request, null);
		System.out.println(resp.getResult());
		Assert.assertFalse(resp.getResult().equals("failure"));
	}

}
