package cxn.pub.pgrls.service;

import cxn.pub.pgrls.Tenant;
import cxn.pub.pgrls.repository.AdminDataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * In a more complete solution, you'd break up your business logic
 * and error handling here and move the data access code to another
 * set of interfaces.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    private JdbcTemplate admin;

    @Autowired
    public AdminServiceImpl(AdminDataSourceRepository adminRepo) {
        admin = new JdbcTemplate(adminRepo.dataSource());
    }

    private JdbcTemplate admin() {
        return admin;
    }

    /**
     * If we want the database to be in charge of its key rather
     * than the client, then we create a chicken and egg problem
     * with RLS because the tenant_id value will be null in the
     * INSERT statement and won't match the current tenant context.
     * We will use the admin connection to run this SQL and it
     * won't have RLS applied.
     * @param tenant
     * @return
     */
    @Override
    public Tenant registerTenant(Tenant tenant) {
        // Have to use named parameters in order to capture
        // the database-generated id
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(admin());
        GeneratedKeyHolder generated = new GeneratedKeyHolder();

        // Building our own SQL because of the enums... yuck
        StringBuilder sql = new StringBuilder("INSERT INTO tenant (name");
        StringBuilder values = new StringBuilder(" VALUES (:name");
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", tenant.getName());
        if (tenant.getStatus() != null) {
            sql.append(", status");
            values.append(", :status");
            params.addValue("status", tenant.getStatus().name(), Types.VARCHAR);
        }
        if (tenant.getTier() != null) {
            sql.append(", tier");
            values.append(", :tier");
            params.addValue("tier", tenant.getTier().name(), Types.VARCHAR);
        }
        sql.append(")");
        values.append(")");
        sql.append(values);

        int update = jdbc.update(sql.toString(), params, generated);
        if (update == 1) {
            UUID tenantId = (UUID) generated.getKeys().get("tenant_id");
            tenant.setId(tenantId);
        } else {
            // todo throw error here?
        }
        return tenant;
    }

    /**
     * Listing all tenants is an admin function. This SQL will run
     * properly under RLS and you'll only get 1 row in the result
     * set -- the one that matches the current tenant context.
     * @return
     */
    @Override
    public List<Tenant> getTenants() {
        List<Tenant> tenants = new ArrayList<>();
        try {
            tenants = admin().query("SELECT tenant_id, name, status, tier FROM tenant", new TenantRowMapper());
        } catch (EmptyResultDataAccessException e) {
            // If row level security policies aren't met, it's not
            // an exception from the database, it's just as if the
            // data didn't exist in the table.
        }
        return tenants;
    }

    @Override
    public void deleteTenant(Tenant tenant) {
        admin().update("DELETE FROM tenant WHERE tenant_id = ?", tenant.getId());
    }

    public void deleteTenantUsers(Tenant tenant) {
        admin().update("DELETE FROM tenant_user WHERE tenant_id = ?", tenant.getId());
    }
}
