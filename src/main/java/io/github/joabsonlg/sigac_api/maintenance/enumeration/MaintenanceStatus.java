package io.github.joabsonlg.sigac_api.maintenance.enumeration;

/**
 * Enum que representa os status possíveis de uma manutenção.
 */
public enum MaintenanceStatus {
    /** Manutenção agendada e ainda não realizada */
    AGENDADA,

    /** Manutenção concluída com sucesso */
    CONCLUIDA,

    /** Manutenção cancelada */
    CANCELADA,

    /** Manutenção em andamento */
    EM_ANDAMENTO
}