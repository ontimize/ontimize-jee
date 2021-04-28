package com.ontimize.jee.webclient.export.executor.support.dataprovider;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.server.rest.QueryParameter;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.pagination.PaginationRequest;
import com.ontimize.jee.webclient.export.pagination.PaginationResult;
import com.ontimize.jee.webclient.export.providers.ExcelExportDataProvider;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;

public class DefaultBareBeanExcelExportPaginatedDataProvider<T> implements ExcelExportDataProvider {

    private static final Integer ROWS_PER_PAGE = 1000;

    List<PropertyDescriptor> descriptors;

    List<T> pageData;

    int pageIndex = -1;

    long totalSize = -1;

    private IntegerProperty rowsPerPage;

    private ObjectProperty<Callback<PaginationRequest, PaginationResult<List<T>>>> pageFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBareBeanExcelExportPaginatedDataProvider.class);

    public DefaultBareBeanExcelExportPaginatedDataProvider() {
        this.descriptors = new ArrayList<>();
        this.rowsPerPageProperty().addListener((o, ov, nv) -> this.reset());
        this.pageFactoryProperty().addListener((o, ov, nv) -> {
            this.reset();
            this.extractFields(this.pageData.get(0));
        });
    }

    public void reset() {
        if (this.pageFactory.get() == null) {
            return;
        }
        final PaginationRequest paginationRequest = new PaginationRequest(1, this.getRowsPerPage(),
                this.getRowsPerPage());
        final PaginationResult<List<T>> paginationResult = this.getPageFactory().call(paginationRequest);
        if (paginationResult != null) {
            this.pageData = paginationResult.getResult();
            this.totalSize = paginationResult.getTotalSize();
            if (this.getRowsPerPage() > this.totalSize) {
                this.setRowsPerPage((int) this.totalSize);
            }
            this.pageIndex = 1;
        }
    }

    public T getData(final int row) {
        int pageOfRow = (row / this.getRowsPerPage()) + 1;
        if (this.pageData == null) {
            this.reset();
            pageOfRow = 0;
        }
        if (pageOfRow != this.pageIndex) {
            this.pageIndex = pageOfRow;
            this.loadPage(this.pageIndex);
        }
        final int localIndex = row % this.getRowsPerPage();
        return this.pageData.get(localIndex);
    }

    private void loadPage(final int pageIndex) {
        final PaginationResult<List<T>> paginationResult = this.getPageFactory()
            .call(new PaginationRequest(pageIndex, this.getRowsPerPage(), 0));
        this.pageData = paginationResult.getResult();
    }

    private static List<PropertyDescriptor> extractDescriptors(final Class clazz) {
        BeanInfo info = null;
        try {
            info = Introspector.getBeanInfo(clazz, Object.class);
            final List<PropertyDescriptor> descriptors = Arrays.asList(info.getPropertyDescriptors());
            final List<PropertyDescriptor> orderedDescriptors = new ArrayList<>();
            // Debido a que BeanInfo devuelve los descriptores en orden alfabético, los ordenamos en el orden
            // en que han sido declarados, que es como los devuelve getDeclaredFields()

            for (final Field field : clazz.getDeclaredFields()) {
                final Optional<PropertyDescriptor> descriptor = descriptors.stream()
                    .filter(f -> f.getName().equals(field.getName()))
                    .findFirst();
                descriptor.ifPresent(orderedDescriptors::add);
            }
            return orderedDescriptors;
        } catch (final IntrospectionException e) {
            LOGGER.error("extractDescriptors", e);
        }
        return Collections.emptyList();
    }

    @Override
    public int getNumberOfRows() {
        return (int) this.totalSize;
    }

    @Override
    public int getNumberOfColumns() {
        return this.descriptors.size();
    }

    @Override
    public Object getCellValue(final int row, final int column) {
        final T object = this.getData(row);
        return this.getFieldValue(object, column);
    }

    public Object getFieldValue(final Object bean, final int fieldNum) {
        final PropertyDescriptor pd = this.descriptors.get(fieldNum);
        final Method getter = pd.getReadMethod();
        try {
            return getter.invoke(bean);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("getFieldValue", e);
        }
        return null;
    }

    public void extractFields(final Object bean) {
        if (bean == null) {
            return;
        }
        this.descriptors = extractDescriptors(bean.getClass());
    }

    @Override
    public int getColumnIndex(final HeadExportColumn column) {
        return this.descriptors.stream()
            .filter(t -> t.getName().equals(column.getId()))
            .map(t -> this.descriptors.indexOf(t))
            .findFirst()
            .orElse(-1);
    }

    public Callback<PaginationRequest, PaginationResult<List<T>>> getPageFactory() {
        return this.pageFactoryProperty().get();
    }

    public void setPageFactory(final Callback<PaginationRequest, PaginationResult<List<T>>> pageFactory) {
        this.pageFactoryProperty().set(pageFactory);
    }

    public ObjectProperty<Callback<PaginationRequest, PaginationResult<List<T>>>> pageFactoryProperty() {
        if (this.pageFactory == null) {
            this.pageFactory = new SimpleObjectProperty<>(this, "pageFactory");
        }

        return this.pageFactory;
    }

    public int getRowsPerPage() {
        return this.rowsPerPageProperty().get();
    }

    public IntegerProperty rowsPerPageProperty() {
        if (this.rowsPerPage == null) {
            this.rowsPerPage = new SimpleIntegerProperty(ROWS_PER_PAGE);
        }
        return this.rowsPerPage;
    }

    public void setRowsPerPage(final int rowsPerPage) {
        this.rowsPerPageProperty().set(rowsPerPage);
    }

    @Override
    public String getService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDao() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueryParameter getQueryParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
