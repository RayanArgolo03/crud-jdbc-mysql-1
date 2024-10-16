package database;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.StandardBasicTypes;

public final class CustomH2Dialect extends H2Dialect {

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        BasicTypeRegistry basicTypeRegistry = functionContributions.getTypeConfiguration().getBasicTypeRegistry();

        functionContributions.getFunctionRegistry().registerPattern("TIME", "FORMATDATETIME(?1, 'HH:mm')",
                basicTypeRegistry.resolve(StandardBasicTypes.LOCAL_TIME));

    }

}
