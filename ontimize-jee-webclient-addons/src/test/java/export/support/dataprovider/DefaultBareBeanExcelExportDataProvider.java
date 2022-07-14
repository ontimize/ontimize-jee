package export.support.dataprovider;

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
import com.ontimize.jee.webclient.export.providers.ExcelExportDataProvider;

public class DefaultBareBeanExcelExportDataProvider<T> implements ExcelExportDataProvider {

    protected final List<T> data;

    List<PropertyDescriptor> descriptors;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBareBeanExcelExportDataProvider.class);

    public DefaultBareBeanExcelExportDataProvider(final List<T> data) {
        this.data = data;
        this.descriptors = new ArrayList<>();
        this.extractFields(data.get(0));
    }

    @Override
    public int getNumberOfRows() {
        if (this.data != null) {
            return this.data.size();
        }
        return 0;
    }

    @Override
    public int getNumberOfColumns() {
        return this.descriptors.size();
    }

    @Override
    public Object getCellValue(final int row, final int column) {
        final T object = this.data.get(row);
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
    public int getColumnIndex(final HeadExportColumn column) {
        return this.descriptors.stream()
            .filter(t -> t.getName().equals(column.getId()))
            .map(t -> this.descriptors.indexOf(t))
            .findFirst()
            .orElse(-1);
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
