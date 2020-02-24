package jason;

public final class Test {
	public static void main(String[] args) throws InstantiationException {
		byte[] buf = "{a:[{x:1,y:2},{x:3,y:4},{x:5,y:6}]}".getBytes();
		Object obj = new JsonParser(buf).parse();
		System.out.println(obj);
	}
}
