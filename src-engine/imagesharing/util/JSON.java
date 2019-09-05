/*
 * Copyright (c) 2019 Felipe Michels Fontoura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 
 * Except as contained in this notice, the name of the above copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization.
 */

package imagesharing.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom JSON parser similar to the global JSON object of JavaScript.
 *
 * @author Felipe Michels Fontoura
 */
public class JSON
{
	public static Object parse(String json) throws IOException
	{
		StringReader reader = new StringReader(json);
		try
		{
			return parse(reader);
		}
		finally
		{
			reader.close();
		}
	}

	public static Object parse(Reader reader) throws IOException
	{
		PushbackReader pushbackReader = new PushbackReader(reader);
		Object result = parseValue(pushbackReader);
		return result;
	}

	private static void skipWhitespaces(PushbackReader pushbackReader) throws IOException
	{
		while (true)
		{
			int character = pushbackReader.read();
			if (character == -1)
			{
				break;
			}
			else if (character != ' ' && character != '\t' && character != '\r' && character != '\n')
			{
				pushbackReader.unread((char) character);
				break;
			}
		}
	}

	private static Object parseValue(PushbackReader pushbackReader) throws IOException
	{
		int character;
		skipWhitespaces(pushbackReader);
		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON value, but found nothing instead!");
		}
		else if (character == '"')
		{
			pushbackReader.unread((char) character);
			return parseString(pushbackReader);
		}
		else if (character == '-' || ('0' <= character && character <= '9'))
		{
			pushbackReader.unread((char) character);
			return parseNumber(pushbackReader);
		}
		else if (character == 't')
		{
			pushbackReader.unread((char) character);
			return parseTrue(pushbackReader);
		}
		else if (character == 'f')
		{
			pushbackReader.unread((char) character);
			return parseFalse(pushbackReader);
		}
		else if (character == 'n')
		{
			pushbackReader.unread((char) character);
			return parseNull(pushbackReader);
		}
		else if (character == '{')
		{
			pushbackReader.unread((char) character);
			return parseObject(pushbackReader);
		}
		else if (character == '[')
		{
			pushbackReader.unread((char) character);
			return parseArray(pushbackReader);
		}
		else
		{
			throw new IOException("Was expecting a JSON value, but got an invalid string instead!");
		}
	}

	private static Map<String, Object> parseObject(PushbackReader pushbackReader) throws IOException
	{
		int character;

		skipWhitespaces(pushbackReader);

		// lê o primeiro '{'.
		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON object, but found nothing instead!");
		}
		else if (character != '{')
		{
			throw new IOException("Was expecting a JSON object, but got something else instead!");
		}

		Map<String, Object> object = new LinkedHashMap<String, Object>();

		// verifica se terminou ('}') ou se tem um par chave-valor.
		skipWhitespaces(pushbackReader);
		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON string, or a '}', but found nothing instead!");
		}
		else if (character == '}')
		{
			return object;
		}
		else
		{
			// devolve o primeiro carácter da string.
			pushbackReader.unread((char) character);

			// lê a chave.
			String key = parseString(pushbackReader);

			// lê e pula o ':'
			skipWhitespaces(pushbackReader);
			character = pushbackReader.read();
			if (character < 0)
			{
				throw new EOFException("Was expecting a ':', but found nothing instead!");
			}
			else if (character != ':')
			{
				throw new IOException("Was expecting a ':', but got something else instead!");
			}

			// lê o valor e armazena no objeto.
			object.put(key, parseValue(pushbackReader));
		}

		while (true)
		{
			skipWhitespaces(pushbackReader);
			character = pushbackReader.read();

			// verifica se terminou ('}') ou se tem uma vírgula (',') seguida de um par chave-valor.
			if (character < 0)
			{
				throw new EOFException("Was expecting a ',', or a '}', but found nothing instead!");
			}
			else if (character == '}')
			{
				return object;
			}
			else if (character == ',')
			{
				// lê a chave.
				String key = parseString(pushbackReader);

				// lê e pula o ':'
				skipWhitespaces(pushbackReader);
				character = pushbackReader.read();
				if (character < 0)
				{
					throw new EOFException("Was expecting a ':', but found nothing instead!");
				}
				else if (character != ':')
				{
					throw new IOException("Was expecting a ':', but got something else instead!");
				}

				// lê o valor e armazena no objeto.
				object.put(key, parseValue(pushbackReader));
			}
			else
			{
				throw new EOFException("Was expecting a JSON string, a ',', or a '}', but found something else instead!");
			}
		}
	}

	private static List<Object> parseArray(PushbackReader pushbackReader) throws IOException
	{
		int character;

		skipWhitespaces(pushbackReader);

		// lê o primeiro '['.
		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON array, but found nothing instead!");
		}
		else if (character != '[')
		{
			throw new IOException("Was expecting a JSON array, but got something else instead!");
		}

		List<Object> array = new ArrayList<Object>();

		// verifica se terminou (']') ou se tem um valor.
		skipWhitespaces(pushbackReader);
		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON value, or a ']', but found nothing instead!");
		}
		else if (character == ']')
		{
			return array;
		}
		else
		{
			// devolve o primeiro carácter do valor.
			pushbackReader.unread((char) character);

			// lê o valor e armazena no array.
			array.add(parseValue(pushbackReader));
		}

		while (true)
		{
			// verifica se terminou (']') ou se tem uma vírgula (',') seguida de um valor.
			skipWhitespaces(pushbackReader);
			character = pushbackReader.read();
			if (character < 0)
			{
				throw new EOFException("Was expecting a ',', or a ']', but found nothing instead!");
			}
			else if (character == ']')
			{
				return array;
			}
			else if (character == ',')
			{
				// lê o valor e armazena no array.
				array.add(parseValue(pushbackReader));
			}
			else
			{
				throw new EOFException("Was expecting a JSON value, a ',', or a ']', but found something else instead!");
			}
		}
	}

	private static Object parseNull(PushbackReader pushbackReader) throws IOException
	{
		final String MODEL = "null";

		skipWhitespaces(pushbackReader);

		for (int i = 0; i < MODEL.length(); i++)
		{
			int character = pushbackReader.read();
			if (character < 0)
			{
				throw new EOFException("Was expecting a '" + MODEL.charAt(i) + "', but found nothing instead!");
			}
			else if (character != MODEL.charAt(i))
			{
				throw new EOFException("Was expecting a '" + MODEL.charAt(i) + "', but found something else instead!");
			}
		}

		return null;
	}

	private static Boolean parseTrue(PushbackReader pushbackReader) throws IOException
	{
		final String MODEL = "true";

		skipWhitespaces(pushbackReader);

		for (int i = 0; i < MODEL.length(); i++)
		{
			int character = pushbackReader.read();
			if (character < 0)
			{
				throw new EOFException("Was expecting a '" + MODEL.charAt(i) + "', but found nothing instead!");
			}
			else if (character != MODEL.charAt(i))
			{
				throw new EOFException("Was expecting a '" + MODEL.charAt(i) + "', but found something else instead!");
			}
		}

		return Boolean.TRUE;
	}

	private static Boolean parseFalse(PushbackReader pushbackReader) throws IOException
	{
		final String MODEL = "false";

		skipWhitespaces(pushbackReader);

		for (int i = 0; i < MODEL.length(); i++)
		{
			int character = pushbackReader.read();
			if (character < 0)
			{
				throw new EOFException("Was expecting a '" + MODEL.charAt(i) + "', but found nothing instead!");
			}
			else if (character != MODEL.charAt(i))
			{
				throw new EOFException("Was expecting a '" + MODEL.charAt(i) + "', but found something else instead!");
			}
		}

		return Boolean.FALSE;
	}

	private static Double parseNumber(PushbackReader pushbackReader) throws IOException
	{
		// baseado na RFC 7159: The JavaScript Object Notation (JSON) Data Interchange Format
		// <https://tools.ietf.org/html/rfc7159>
		// Mais especificamente, a seção 6 da RFC.
		// <https://tools.ietf.org/html/rfc7159#section-6>

		int character;

		skipWhitespaces(pushbackReader);

		StringBuilder builder = new StringBuilder();

		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON number, but found nothing instead!");
		}
		else if (character == '-')
		{
			builder.append((char) character);
		}
		else
		{
			pushbackReader.unread((char) character);
		}

		boolean dot = false;

		character = pushbackReader.read();
		if (character == -1)
		{
			throw new EOFException("Was expecting a JSON number, but found nothing instead!");
		}
		else if ('0' <= character && character <= '9')
		{
			builder.append((char) character);
		}
		else
		{
			throw new EOFException("Invalid character in JSON number!");
		}

		while (true)
		{
			character = pushbackReader.read();
			if ('0' <= character && character <= '9')
			{
				builder.append((char) character);
			}
			else if (character == 'e')
			{
				builder.append((char) character);
				break;
			}
			else if (character == '.')
			{
				dot = true;
				builder.append((char) character);
				break;
			}
			else
			{
				if (character != -1)
				{
					pushbackReader.unread((char) character);
				}
				return Double.parseDouble(builder.toString());
			}
		}

		if (dot)
		{
			character = pushbackReader.read();
			if (character == -1)
			{
				throw new EOFException("Was expecting a JSON number, but found nothing instead!");
			}
			else if ('0' <= character && character <= '9')
			{
				builder.append((char) character);
			}
			else
			{
				throw new EOFException("Invalid character in JSON number!");
			}

			while (true)
			{
				character = pushbackReader.read();
				if ('0' <= character && character <= '9')
				{
					builder.append((char) character);
				}
				else if (character == 'e')
				{
					builder.append((char) character);
					break;
				}
				else
				{
					if (character != -1)
					{
						pushbackReader.unread((char) character);
					}

					return Double.parseDouble(builder.toString());
				}
			}
		}

		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON number, but found nothing instead!");
		}
		else if (character == '-' || character == '+')
		{
			builder.append((char) character);
		}
		else
		{
			pushbackReader.unread((char) character);
		}

		character = pushbackReader.read();
		if (character == -1)
		{
			throw new EOFException("Was expecting a JSON number, but found nothing instead!");
		}
		else if ('0' <= character && character <= '9')
		{
			builder.append((char) character);
		}
		else
		{
			throw new EOFException("Invalid character in JSON number!");
		}

		while (true)
		{
			character = pushbackReader.read();
			if ('0' <= character && character <= '9')
			{
				builder.append((char) character);
			}
			else
			{
				if (character != -1)
				{
					pushbackReader.unread((char) character);
				}
				return Double.parseDouble(builder.toString());
			}
		}
	}

	private static String parseString(PushbackReader pushbackReader) throws IOException
	{
		// baseado na RFC 7159: The JavaScript Object Notation (JSON) Data Interchange Format
		// <https://tools.ietf.org/html/rfc7159>
		// Mais especificamente, a seção 7 da RFC.
		// <https://tools.ietf.org/html/rfc7159#section-7>

		int character;

		skipWhitespaces(pushbackReader);

		StringBuilder builder = new StringBuilder();

		character = pushbackReader.read();
		if (character < 0)
		{
			throw new EOFException("Was expecting a JSON string, but found nothing instead!");
		}
		else if (character != '"')
		{
			throw new IOException("Was expecting a JSON string, but got something else instead!");
		}

		while (true)
		{
			character = pushbackReader.read();
			if (character == -1)
			{
				throw new IOException("JSON string ended abruptly!");
			}
			else if (0x0000 <= character && character <= 0x001F)
			{
				throw new IOException("Invalid character in JSON string!");
			}
			else if (character == '"')
			{
				return builder.toString();
			}
			else if (character != '\\')
			{
				builder.append((char) character);
			}
			else
			{
				character = pushbackReader.read();
				if (character == -1)
				{
					throw new IOException("JSON string ended abruptly!");
				}
				switch (character)
				{
					case '"':
						builder.append('"');
						break;
					case '\\':
						builder.append('\\');
						break;
					case '/':
						builder.append('/');
						break;
					case 'b':
						builder.append('\b');
						break;
					case 'f':
						builder.append('\f');
						break;
					case 'n':
						builder.append('\n');
						break;
					case 'r':
						builder.append('\r');
						break;
					case 't':
						builder.append('\t');
						break;
					case 'u':
						int codePoint = 0;
						for (int i = 0; i < 4; i++)
						{
							character = pushbackReader.read();
							if ('0' <= character && character <= '9')
							{
								codePoint = (codePoint << 4) + (character - '0');
							}
							else if ('a' <= character && character <= 'f')
							{
								codePoint = (codePoint << 4) + (character + 10 - 'a');
							}
							else if ('A' <= character && character <= 'F')
							{
								codePoint = (codePoint << 4) + (character + 10 - 'A');
							}
							else
							{
								throw new IOException("Invalid hexadecimal in unicode escape sequence in JSON string!");
							}
						}
						builder.append((char) codePoint);
						break;
					default:
						throw new IOException("Invalid escape sequence in JSON string!");
				}
			}
		}
	}
}
