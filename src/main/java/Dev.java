
import io.github.fabricetheytaz.schema.org.database.SchemaOrgDatabase;

public class Dev
	{
	public static void main(String[] args) throws Exception
		{
		//final Preferences preferences = Preferences.userNodeForPackage(SchemaOrgDatabase.class);
		//preferences.put("path", "/home/thefab/Documents/Code/Database/schema-org.sqlite");
		//preferences.flush();

		try (SchemaOrgDatabase database = new SchemaOrgDatabase())
			{
			//System.out.println(database.getAllAsJSON());
			System.out.println(database.getAll());
			}
		}
	}
