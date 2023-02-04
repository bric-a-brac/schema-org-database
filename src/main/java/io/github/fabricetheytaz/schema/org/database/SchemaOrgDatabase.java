package io.github.fabricetheytaz.schema.org.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.function.FailableBiConsumer;
import com.google.gson.Gson;
import io.github.fabricetheytaz.schema.org.types.Thing;

import static io.github.fabricetheytaz.util.Argument.notNull;

/**
 * @version 0.1.0
 * @since 0.1.0
 */
public class SchemaOrgDatabase implements AutoCloseable
	{
	private static final Gson GSON = new Gson();

	private static final String CONNECTION_STRING = "jdbc:sqlite:%s";

	private static final String SQL_INSERT_THING = "INSERT INTO `thing` (`type`, `json`) VALUES (?, JSON(?))";
	private static final String SQL_SELECT_THINGS_JSON = "SELECT `json` FROM `thing`";
	//private static final String SQL_SELECT_THINGS_BY_TYPE = "SELECT `id`, JSON_EXTRACT(`json`, '$.@type') AS 'type', `json` FROM `thing` WHERE `type` = ?";
	private static final String SQL_SELECT_THINGS_BY_TYPE = "SELECT `id`, `json` FROM `thing` WHERE `type` = ?";
	private static final String SQL_UPDATE_THINGS_IDS = "UPDATE `thing` SET `json` = JSON_SET(`json`, '$.@id', `id`)";

	private final Connection connection;

	/**
	 * Chemin pris depuis les préférences
	 * 
	 * @since 0.1.0
	 */
	public SchemaOrgDatabase() throws SQLException
		{
		this(getDatabasePath());
		}

	/**
	 * @since 0.1.0
	 */
	public SchemaOrgDatabase(final String path) throws SQLException
		{
		super();

		connection = DriverManager.getConnection(String.format(CONNECTION_STRING, notNull(path)));
		}

	/**
	 * @since 0.1.0
	 */
	public final <T extends Thing> boolean insert(final T thing, final FailableBiConsumer<Integer, T, IOException> consumer) throws IOException, SQLException
		{
		final String json = GSON.toJson(notNull(thing));

		try (final PreparedStatement statement = connection.prepareStatement(SQL_INSERT_THING, Statement.RETURN_GENERATED_KEYS))
			{
			statement.setString(1, thing.getType());
			statement.setString(2, json);

			final int count = statement.executeUpdate();

			if (consumer != null)
				{
				consumer.accept(getLastInsertId(statement), thing);
				}

			return (count == 1);
			}
		}

	/**
	 * @since 0.1.0
	 */
	public final <T extends Thing> boolean insert(final T thing) throws IOException, SQLException
		{
		return insert(thing, null);
		}

	/**
	 * @since 0.1.0
	 */
	public final int getCount() throws SQLException
		{
		final Statement statement = connection.createStatement();

		try (final ResultSet things = statement.executeQuery("SELECT COUNT(*) AS 'total' FROM `thing`"))
			{
			if (things.next())
				{
				return things.getInt("total");
				}
			}

		return -1;
		}

	/**
	 * @since 0.1.0
	 */
	public final <T extends Thing> void getAll(final Class<T> classOfT, final FailableBiConsumer<Integer, T, IOException> consumer) throws IOException, SQLException
		{
		notNull(classOfT);
		notNull(consumer);

		final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_THINGS_BY_TYPE);

		// FIXME: Fonctionne uniquement pour l'instant si Class = @type !!!!!!!!!
		// TODO: Annotation.getAnnotation()... en cours dans SchemaOrg Types
		statement.setString(1, classOfT.getSimpleName());

		try (final ResultSet things = statement.executeQuery())
			{
			while (things.next())
				{
				final Integer id = Integer.valueOf(things.getInt("id"));
				final String json = things.getString("json");

				final T thing = GSON.fromJson(json, classOfT);

				consumer.accept(id, thing);
				}
			}
		}

	/**
	 * @since 0.1.0
	 */
	public final void getAll(final Consumer<String> consumer) throws SQLException
		{
		notNull(consumer);

		final Statement statement = connection.createStatement();

		try (final ResultSet things = statement.executeQuery(SQL_SELECT_THINGS_JSON))
			{
			while (things.next())
				{
				consumer.accept(things.getString("json"));
				}
			}
		}

	/**
	 * @since 0.1.0
	 */
	public final String getAll() throws SQLException
		{
		final StringJoiner array = new StringJoiner(",\n", "[\n", "\n]");

		getAll(json -> array.add(json));

		return array.toString();
		}

	/**
	 * @since 0.1.0
	 */
	public final int updateIDs() throws SQLException
		{
		return connection.createStatement().executeUpdate(SQL_UPDATE_THINGS_IDS);
		}

	public final void query(final IQuery query) throws SQLException
		{
		throw new UnsupportedOperationException("SchemaOrgDatabase::query()");
		}

	/**
	 * @since 0.1.0
	 */
	private final Integer getLastInsertId(final Statement statement) throws SQLException
		{
		try (final ResultSet keys = statement.getGeneratedKeys())
			{
			if (keys.next())
				{
				return Integer.valueOf(keys.getInt(1));
				}
			}

		return null;
		}

	/**
	 * @since 0.1.0
	 */
	@Override
	public final void close() throws SQLException
		{
		connection.close();
		}

	/**
	 * @since 0.1.0
	 */
	private static final String getDatabasePath()
		{
		final Preferences preferences = Preferences.userNodeForPackage(SchemaOrgDatabase.class);

		// FIXME: Error si null !!
		return preferences.get("path", null);
		}
	}
