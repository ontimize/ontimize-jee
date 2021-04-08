package com.ontimize.jee.server.spring.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.aop.ClassFilter;

public class PackageClassFilter implements ClassFilter {

    private String packagePrefix;

    private Pattern packagePrefixPtrn;

    private List<String> packagePrefixes;

    private List<Pattern> packagePrefixPtrns;

    private String classRegexp;

    private Pattern classRegexpPtrn;

    private List<String> classRegexps;

    private List<Pattern> classRegexpPtrns;

    @Override
    public boolean matches(final Class<?> clazz) {
        Package p = clazz.getPackage();
        String pkName = "";
        if (p != null) {
            pkName = p.getName();
        }

        boolean matchPkg = false;
        if ((this.packagePrefix != null) && (this.packagePrefixPtrn != null)
                && this.packagePrefixPtrn.matcher(pkName).matches()) {
            matchPkg = true;
        }
        if (!matchPkg && (this.packagePrefixes != null) && (this.packagePrefixPtrns != null)) {
            for (final Pattern ppp : this.packagePrefixPtrns) {
                if ((ppp != null) && ppp.matcher(pkName).matches()) {
                    matchPkg = true;
                }
            }
        }

        if (matchPkg) {
            return (this.classRegexp == null) || this.evalClassName(clazz.getCanonicalName().replace(pkName, ""));
        } else if ((this.packagePrefix == null) && (this.packagePrefixes == null) && (this.classRegexp != null)) {
            return this.evalClassName(clazz.getCanonicalName().replace(pkName, ""));
        }

        return false;
    }

    private boolean evalClassName(final String clName) {
        boolean matchPkg = false;
        if ((this.classRegexp != null) && (this.classRegexpPtrn != null)
                && this.classRegexpPtrn.matcher(clName).matches()) {
            matchPkg = true;
        }
        if (!matchPkg && (this.classRegexps != null) && (this.classRegexpPtrns != null)) {
            for (final Pattern ppp : this.classRegexpPtrns) {
                if ((ppp != null) && ppp.matcher(clName).matches()) {
                    matchPkg = true;
                }
            }
        }
        if (matchPkg || ((this.classRegexp == null) && (this.classRegexps == null))) {
            matchPkg = true;
        }
        return false;
    }

    public void setPackageRegexp(final String packageRegexp) {
        this.packagePrefix = packageRegexp;
        this.packagePrefixPtrn = Pattern.compile(packageRegexp);
    }

    public void setPackageRegexps(final List<String> packageRegexps) {
        this.packagePrefixes = packageRegexps;
        if (packageRegexps != null) {
            this.packagePrefixPtrns = new ArrayList<>();
            for (final String regexp : packageRegexps) {
                this.packagePrefixPtrns.add(Pattern.compile(regexp));
            }
        } else {
            this.packagePrefixPtrns = null;
        }
    }

    public void setClassRegexp(final String classRegexp) {
        this.classRegexp = classRegexp;
        this.classRegexpPtrn = Pattern.compile(classRegexp);
    }

    public void setClassRegexps(final List<String> classRegexps) {
        this.classRegexps = classRegexps;
        if (classRegexps != null) {
            this.classRegexpPtrns = new ArrayList<>();
            for (final String regexp : classRegexps) {
                this.classRegexpPtrns.add(Pattern.compile(regexp));
            }
        } else {
            this.classRegexpPtrns = null;
        }
    }

}
