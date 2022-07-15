package export.helpers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ontimize.jee.webclient.export.ExportColumn;
import com.ontimize.jee.webclient.export.ExportColumnStyle;
import com.ontimize.jee.webclient.export.HeadExportColumn;
import com.ontimize.jee.webclient.export.providers.ExportColumnProvider;
import com.ontimize.jee.webclient.export.support.BaseExportColumnProvider;
import com.ontimize.jee.webclient.export.support.DefaultExportColumnStyle;
import com.ontimize.jee.webclient.export.support.DefaultHeadExportColumn;
import com.ontimize.jee.webclient.export.util.ColumnCellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BareBeanExportHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BareBeanExportHelper.class);

    private BareBeanExportHelper() {

    }

    public static <T> ExportColumnProvider getExportContextFromBean(final Class<T> beanClass) {
        final List<HeadExportColumn> headerColumns = createHeaderColumnsFromClass(beanClass);
        final List<ExportColumn> bodyColumns = createBodyColumnsFromClass(beanClass);
        return new BaseExportColumnProvider(headerColumns, bodyColumns);
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
            LOGGER.error("ExtractDescriptors", e);
        }
        return Collections.emptyList();
    }

    private static <T> List<ExportColumn> createBodyColumnsFromClass(final Class<T> beanClass) {
        final List<ExportColumn> columns = new ArrayList<>();

        final List<PropertyDescriptor> descriptors = extractDescriptors(beanClass);
        for (final PropertyDescriptor pd : Objects.requireNonNull(descriptors)) {
            columns.add(new ExportColumn<>(
                    pd.getName(),
                    pd.getName(),
                    pd.getName().length(),
                    createBodyCellStyleFromClass(pd.getPropertyType())));
        }
        return columns;
    }

    private static <T> List<HeadExportColumn> createHeaderColumnsFromClass(final Class<T> beanClass) {
        final List<HeadExportColumn> columns = new ArrayList<>();
        final List<PropertyDescriptor> descriptors = extractDescriptors(beanClass);
        for (final PropertyDescriptor pd : Objects.requireNonNull(descriptors)) {
            columns.add(new DefaultHeadExportColumn(
                    pd.getName(),
                    pd.getName(),
                    createDefaultHeaderCellStyle()));

        }
        return columns;
    }

    private static ExportColumnStyle createDefaultHeaderCellStyle() {
        final ExportColumnStyle style = new DefaultExportColumnStyle();
        style.setAlignment(ExportColumnStyle.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(ExportColumnStyle.VerticalAlignment.CENTER);
        return style;
    }

    private static <T> ExportColumnStyle createBodyCellStyleFromClass(final Class<T> beanClass) {
        final ExportColumnStyle style = new DefaultExportColumnStyle();
        if (ColumnCellUtils.isNumber(beanClass)) {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.RIGHT);
            if (Double.class.isAssignableFrom(beanClass)
                    || double.class.isAssignableFrom(beanClass)
                    || Float.class.isAssignableFrom(beanClass)
                    || float.class.isAssignableFrom(beanClass)) {
                style.setDataFormatString("#,##0.00");
            } else if (Long.class.isAssignableFrom(beanClass)
                    || long.class.isAssignableFrom(beanClass)
                    || Integer.class.isAssignableFrom(beanClass)
                    || int.class.isAssignableFrom(beanClass)) {
                style.setDataFormatString("#,##0");
            }
        } else if (ColumnCellUtils.isBoolean(beanClass) || boolean.class.isAssignableFrom(beanClass)) {
            style.setDataFormatString("text");

        } else if (ColumnCellUtils.isDate(beanClass)) {
            style.setDataFormatString("dd/mm/yyyy");
        } else {
            style.setAlignment(ExportColumnStyle.HorizontalAlignment.LEFT);
        }
        return style;
    }

}
