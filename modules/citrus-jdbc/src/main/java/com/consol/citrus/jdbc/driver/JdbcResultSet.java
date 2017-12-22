/*
 * Copyright 2006-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.jdbc.driver;

import com.consol.citrus.jdbc.model.ResultSet;
import com.consol.citrus.util.TypeConversionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcResultSet implements java.sql.ResultSet {

    /** Remote ResultSet */
    private ResultSet resultSet;

    //The current ResultSet data row
    private ResultSet.Row row;

    /**
     * Constructor using remote result set.
     */
    public JdbcResultSet(ResultSet resultSet) throws SQLException {
        this.resultSet = resultSet;
    }

    @Override
    public boolean next() throws SQLException {
        try {
            row = resultSet.getNextRow();
        } catch(SQLException ex) {
            throw ex;
        } catch(Exception ex) {
            return false;
        }

        return row != null;
    }

    @Override
    public void close()	throws SQLException {
        resultSet.close();
    }

    public String getString(int columnIndex) throws SQLException {
        Object columnData = row.getValues().get(columnIndex-1);

        if (columnData == null) {
            return null;
        } else {
            return (String)columnData;
        }
    }

    public String getString(String columnName) throws SQLException {
        Object columnData = row.getValues().get(findColumn(columnName));
        if (columnData == null) {
            return null;
        } else {
            return (String)columnData;
        }
    }

    public float getFloat(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null) {
            return 0;
        } else {
            String flotObj = (String)row.getValues().get(columnIndex-1);
            return Float.valueOf(flotObj);
        }
    }

    public float getFloat(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return 0;
        } else {
            String flotObj = row.getValues().get(findColumn(columnName));
            return Float.valueOf(flotObj);
        }
    }

    public int getInt(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null) {
            return 0;
        } else {
            String currentObj =(String)row.getValues().get(columnIndex-1);
            return Integer.valueOf(currentObj);
        }
    }

    public int getInt(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return 0;
        } else {
            String currentObj = row.getValues().get(findColumn(columnName));
            return Integer.valueOf(currentObj);
        }
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1) == null) {
            return false;
        } else {
            String boolObj = (String)row.getValues().get(columnIndex-1);
            return Boolean.valueOf(boolObj);
        }
    }

    public byte getByte(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null) {
            return 0;
        } else {
            String byeObj =(String)row.getValues().get(columnIndex-1);
            return Byte.valueOf(byeObj);
        }
    }

    public short getShort(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null) {
            return 0;
        } else {
            String sortObj = (String)row.getValues().get(columnIndex-1);
            return Short.valueOf(sortObj);
        }
    }

    public long getLong(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null) {
            return 0;
        } else {
            String langObj = (String)row.getValues().get(columnIndex-1);
            return Long.valueOf(langObj);
        }
    }

    public double getDouble(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null) {
            return 0;
        } else {
            String dubObj = (String)row.getValues().get(columnIndex-1);
            return Double.valueOf(dubObj);
        }
    }

    public BigDecimal getBigDecimal(int columnIndex,int scale) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        return TypeConversionUtils.convertIfNecessary(row.getValues().get(columnIndex-1), byte[].class);
    }

    public java.sql.Date getDate(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        String datObj = (String)row.getValues().get(columnIndex-1);
        return java.sql.Date.valueOf(datObj);
    }

    public java.sql.Time getTime(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        String timObj = (String)row.getValues().get(columnIndex-1);
        return java.sql.Time.valueOf(timObj);
    }

    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        String timstmpObj = (String)row.getValues().get(columnIndex-1);
        return java.sql.Timestamp.valueOf(timstmpObj);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(columnIndex-1), byte[].class);
        return new ByteArrayInputStream(byteArray);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(columnIndex-1), byte[].class);
        return new ByteArrayInputStream(byteArray);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(columnIndex-1), byte[].class);
        return new ByteArrayInputStream(byteArray);
    }

    public Object getObject(int columnIndex) throws SQLException {
        return row.getValues().get(columnIndex-1);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        String bigdStr = (String)row.getValues().get(columnIndex-1);
        Long bigdObj = Long.valueOf(bigdStr);
        return BigDecimal.valueOf(bigdObj);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return false;
        } else {
            String currentObj = row.getValues().get(findColumn(columnName));
            return Boolean.valueOf(currentObj);
        }
    }

    public byte getByte(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return 0;
        } else {
            String byeObj = row.getValues().get(findColumn(columnName));
            return Byte.valueOf(byeObj);
        }
    }

    public short getShort(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return 0;
        } else {
            String sortObj = row.getValues().get(findColumn(columnName));
            return Short.valueOf(sortObj);
        }
    }

    public long getLong(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return 0;
        } else {
            String langObj = row.getValues().get(findColumn(columnName));
            return Long.valueOf(langObj);
        }
    }

    public double getDouble(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return 0;
        } else {
            String dubObj = row.getValues().get(findColumn(columnName));
            return Double.valueOf(dubObj);
        }
    }

    public BigDecimal getBigDecimal(String columnName,int scale) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public byte[] getBytes(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null) {
            return null;
        } else {
            return TypeConversionUtils.convertIfNecessary(row.getValues().get(findColumn(columnName)), byte[].class);
        }
    }

    public java.sql.Date getDate(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        String dateObj = row.getValues().get(findColumn(columnName));
        return java.sql.Date.valueOf(dateObj);
    }

    public Time getTime(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        String timObj = row.getValues().get(findColumn(columnName));
        return java.sql.Time.valueOf(timObj);
    }

    public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        String timstmpObj = row.getValues().get(findColumn(columnName));
        return java.sql.Timestamp.valueOf(timstmpObj);
    }

    public Object getObject(String columnName) throws SQLException {
        return row.getValues().get(findColumn(columnName));
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        String bigdStr = row.getValues().get(findColumn(columnName));
        Long bigdObj = Long.valueOf(bigdStr);
        return BigDecimal.valueOf(bigdObj);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(findColumn(columnName)), byte[].class);
        return new ByteArrayInputStream(byteArray);
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(findColumn(columnName)), byte[].class);
        return new ByteArrayInputStream(byteArray);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(findColumn(columnName)), byte[].class);
        return new ByteArrayInputStream(byteArray);
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public void clearWarnings() throws SQLException {
    }

    public String getCursorName() throws SQLException {
        return "";
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return new JdbcResultSetMetaData(resultSet);
    }

    public int findColumn(String columnName) throws SQLException {
        return resultSet.getColumns().indexOf(resultSet.getColumns().stream().filter(column -> column.getName().equals(columnName)).findFirst().orElse(new ResultSet.Column()));
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        if (row.getValues().get(columnIndex-1)==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(columnIndex-1), byte[].class);
        return new InputStreamReader(new ByteArrayInputStream(byteArray));
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        if (row.getValues().get(findColumn(columnName))==null)
            return null;

        byte[] byteArray = TypeConversionUtils.convertIfNecessary(row.getValues().get(findColumn(columnName)), byte[].class);
        return new InputStreamReader(new ByteArrayInputStream(byteArray));
    }

    public boolean isBeforeFirst() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean isAfterLast() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean isFirst() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean isLast() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public void beforeFirst() throws SQLException {
    }

    public void afterLast() throws SQLException {
    }

    public boolean first() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean last() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public int getRow() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean absolute(int row) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean relative(int rows) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean previous() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public void setFetchDirection(int direction) throws SQLException {
    }

    public int getFetchDirection() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public void setFetchSize(int rows) throws SQLException {
    }

    public int getFetchSize() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public int getType() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public int getConcurrency() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean rowUpdated() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean rowInserted() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean rowDeleted() throws SQLException {
        throw new SQLException("Not Supported");
    }

    public void updateNull(int columnIndex) throws SQLException {
    }

    public void updateBoolean(int columnIndex,boolean x) throws SQLException {
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
    }

    public void updateShort(int columnIndex,short x) throws SQLException {
    }

    public void updateInt(int columnIndex,int x) throws SQLException {
    }

    public void updateLong(int columnIndex,long x) throws SQLException {
    }

    public void updateFloat(int columnIndex,float x) throws SQLException {
    }

    public void updateDouble(int columnIndex,double x) throws SQLException {
    }

    public void updateBigDecimal(int columnIndex,BigDecimal x) throws SQLException {
    }

    public void updateString(int columnIndex,String x) throws SQLException {
    }

    public void updateBytes(int columnIndex,byte[] x) throws SQLException {
    }

    public void updateDate(int columnIndex,java.sql.Date x) throws SQLException {
    }

    public void updateTime(int columnIndex,Time x) throws SQLException {
    }

    public void updateTimestamp(int columnIndex,Timestamp x) throws SQLException {
    }

    public void updateBinaryStream(int columnIndex,InputStream x,int length) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex,Reader x,int length) throws SQLException {
    }

    public void updateObject(int columnIndex,Object x,int scale) throws SQLException {
    }

    public void updateObject(int columnIndex,Object x) throws SQLException {
    }

    public void updateNull(String columnName) throws SQLException {
    }

    public void updateByte(String columnName, byte x) throws SQLException {
    }

    public void updateShort(String columnName, short x) throws SQLException {
    }

    public void updateInt(String columnName,int x) throws SQLException {
    }

    public void updateLong(String columnName,long x) throws SQLException {
    }

    public void updateFloat(String columnName, float x) throws SQLException {
    }

    public void updateDouble(String columnName,double x) throws SQLException {
    }

    public void updateBigDecimal(String columnName,BigDecimal x) throws SQLException {
    }

    public void updateString(String columnName,String x) throws SQLException {
    }

    public void updateBytes(String columnName,byte[] x) throws SQLException {
    }

    public void updateDate(String columnName,java.sql.Date x) throws SQLException {
    }

    public void updateTime(String columnName, Time x) throws SQLException {
    }

    public void updateTimestamp(String columnName,Timestamp x) throws SQLException {
    }

    public void updateAsciiStream(String columnName,InputStream x,int length) throws SQLException {
    }

    public void updateBinaryStream(String columnName,InputStream x,int length) throws SQLException {
    }

    public void updateCharacterStream(String columnName,Reader reader,int length) throws SQLException {
    }

    public void updateObject(String columnName,Object x,int scale) throws SQLException {
    }

    public void updateObject(String columnName,Object x) throws SQLException {
    }

    public void insertRow() throws SQLException {
    }

    public void updateRow()throws SQLException {
    }

    public void deleteRow()  throws SQLException {
    }

    public void refreshRow()  throws SQLException {
    }

    public void cancelRowUpdates() throws SQLException {
    }

    public void moveToInsertRow() throws SQLException {
    }

    public void moveToCurrentRow() throws SQLException {
    }

    public Statement getStatement()  throws SQLException {
        throw new SQLException("Not Supported");
    }


    public java.sql.Date getDate(int columnIndex,Calendar cal) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public java.sql.Date getDate(String columnName,Calendar cal) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Time getTime(int columnIndex,Calendar cal) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Time getTime(String columnName,Calendar cal) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Timestamp getTimestamp(int columnIndex,Calendar cal) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Timestamp getTimestamp(String columnName,Calendar cal) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException  {
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException  {
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException  {
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException  {
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException  {
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException  {
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException  {
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException  {
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException  {
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException  {
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException  {
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException  {
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException  {
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException  {
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException  {
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException  {
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException  {
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException  {
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException  {
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException  {
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException  {
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException  {
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException  {
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException  {
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException  {
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException  {
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException  {
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException  {
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException  {
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException  {
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException  {
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException  {
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException  {
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException  {
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException  {
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException  {
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException  {
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException  {
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException  {
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException  {
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException  {
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException  {
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException  {
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public boolean wasNull()throws SQLException {
        throw new SQLException("Not Supported");
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
    }


    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    }

    public Object getObject(int i, Map map) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Ref getRef(int i) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Blob getBlob(int i) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Clob getClob(int i) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Array getArray(int i) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Object getObject(String colName, Map map) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Ref getRef(String colName) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Blob getBlob(String colName) throws SQLException {
        throw new SQLException("Not Supported");
    }

    public Clob getClob(String colName) throws SQLException {
        throw new SQLException("Not Supported");
    }
    public Array getArray(String colName) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Not Supported");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("Not Supported");
    }
}