package io.github.fabricetheytaz.schema.org.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.lang3.function.FailableBiConsumer;
import com.google.gson.Gson;
import io.github.fabricetheytaz.schema.org.Thing;

import static io.github.fabricetheytaz.util.Argument.notNull;

/**
 * @version 0.1.0
 * @since 0.1.0
 */
public class SchemaOrgDatabase implements AutoCloseable
	{
	private static final Gson GSON = new Gson();

	private static final String CONNECTION_STRING = "jdbc:sqlite:%s";

	private static final String SQL_INSERT_THING = "INSERT INTO `thing` (`json`) VALUES (JSON(?))";
	private static final String SQL_SELECT_THINGS_JSON = "SELECT `json` FROM `thing`";
	//private static final String SQL_SELECT_THINGS = "SELECT `id`, JSON_EXTRACT(`json`, '$.@type') AS 'type', `json` FROM `thing`";
	private static final String SQL_SELECT_THINGS_BY_TYPE = "SELECT `id`, JSON_EXTRACT(`json`, '$.@type') AS 'type', `json` FROM `thing` WHERE `type` = ?";

	private final Connection connection;

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
	public final int insert(final Thing thing, final FailableBiConsumer<Integer, Thing, IOException> consumer) throws SQLException, IOException
		{
		final String json = GSON.toJson(notNull(thing));

		try (final PreparedStatement statement = connection.prepareStatement(SQL_INSERT_THING, Statement.RETURN_GENERATED_KEYS))
			{
			statement.setString(1, json);

			final int count = statement.executeUpdate();

			if (consumer != null)
				{
				consumer.accept(getLastInsertId(statement), thing);
				}

			return count;
			}
		}

	/**
	 * @since 0.1.0
	 */
	public final int insert(final Thing thing) throws SQLException, IOException
		{
		return insert(thing, null);
		}

	/**
	 * @since 0.1.0
	 */
	public final <T extends Thing> void getAll(final Class<T> classOfT, final FailableBiConsumer<Integer, T, IOException> consumer) throws SQLException, IOException
		{
		notNull(classOfT);
		notNull(consumer);

		final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_THINGS_BY_TYPE);

		// FIXME: Fonctionne uniquement pour l'instant si Class = @type !!!!!!!!!
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
	public String getAllAsJSON() throws SQLException
		{
		final StringBuilder json = new StringBuilder();

		json.append("[\n");

		final Statement statement = connection.createStatement();

		try (final ResultSet things = statement.executeQuery(SQL_SELECT_THINGS_JSON))
			{
			while (things.next())
				{
				json.append(things.getString("json"));

				/*
				// Pas supporté par SQLite
				if (!things.isLast())
					{
					json.append(",\n");
					}
				*/

				json.append(",\n");
				}
			}

		// Supprimer la dernière virgule :) et \n
		json.deleteCharAt(json.length() - 1);
		json.deleteCharAt(json.length() - 1);

		json.append("\n]");

		return json.toString();
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
	public final void close()
		{
		try
			{
			connection.close();
			}
		catch (final SQLException ex)
			{
			throw new RuntimeException(ex);
			}
		}
	}
