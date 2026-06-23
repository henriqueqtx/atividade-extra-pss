package com.ufes.delivery.repository;

import com.ufes.delivery.model.CupomDescontoPedido;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class CupomRepositoryEmMemoria implements ICupomRepository {
    private Map<String, CupomDescontoPedido> cuponsDisponiveis = new HashMap<>();

    public CupomRepositoryEmMemoria() {
        cuponsDisponiveis.put("DESC10", new CupomDescontoPedido("DESC10", 10.0, LocalDateTime.of(2026, 4, 25, 0, 0),
                LocalDateTime.of(2026, 4, 27, 23, 59)));
        cuponsDisponiveis.put("DESC20", new CupomDescontoPedido("DESC20", 20.0, LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 5, 5, 23, 59)));
        cuponsDisponiveis.put("DESC30", new CupomDescontoPedido("DESC30", 30.0, LocalDateTime.of(2026, 4, 24, 0, 0),
                LocalDateTime.of(2026, 4, 24, 23, 59)));
        cuponsDisponiveis.put("DIAPAI12", new CupomDescontoPedido("DIAPAI12", 12.0, LocalDateTime.of(2026, 5, 9, 0, 0),
                LocalDateTime.of(2026, 5, 10, 23, 59)));
        cuponsDisponiveis.put("DIAMAE12", new CupomDescontoPedido("DIAMAE12", 12.0, LocalDateTime.of(2026, 5, 10, 0, 0),
                LocalDateTime.of(2026, 5, 12, 23, 59)));
        cuponsDisponiveis.put("NATAL10", new CupomDescontoPedido("NATAL10", 10.0, LocalDateTime.of(2026, 4, 20, 0, 0),
                LocalDateTime.of(2026, 4, 26, 23, 59)));
        cuponsDisponiveis.put("FESTA15", new CupomDescontoPedido("FESTA15", 15.0, LocalDateTime.of(2026, 4, 30, 18, 0),
                LocalDateTime.of(2026, 5, 1, 6, 0)));
        cuponsDisponiveis.put("BLACK50", new CupomDescontoPedido("BLACK50", 50.0, LocalDateTime.of(2026, 4, 28, 0, 0),
                LocalDateTime.of(2026, 4, 28, 23, 59)));
    }

    public void adicionarCupom(CupomDescontoPedido cupom) {
        validarCupom(cupom);

        if (cuponsDisponiveis.containsKey(cupom.getCodigo())) {
            throw new IllegalStateException("Ja existe cupom com codigo " + cupom.getCodigo());
        }

        cuponsDisponiveis.put(cupom.getCodigo(), cupom);
    }

    public void atualizarCupom(CupomDescontoPedido cupom) {
        validarCupom(cupom);

        if (!cuponsDisponiveis.containsKey(cupom.getCodigo())) {
            throw new IllegalArgumentException("Cupom inexistente: " + cupom.getCodigo());
        }

        cuponsDisponiveis.put(cupom.getCodigo(), cupom);
    }

    public void removerCupom(String codigo) {
        if (buscarCupom(codigo).isEmpty()) {
            throw new IllegalArgumentException("Cupom inexistente: " + codigo);
        }

        cuponsDisponiveis.remove(codigo);
    }

    public void removerCuponsExpirados() {
        removerCuponsExpirados(LocalDate.now());
    }

    public void removerCuponsExpirados(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            throw new IllegalArgumentException("Data de referencia nao pode ser nula");
        }

        Iterator<Map.Entry<String, CupomDescontoPedido>> iterator = cuponsDisponiveis.entrySet().iterator();

        while (iterator.hasNext()) {
            CupomDescontoPedido cupom = iterator.next().getValue();

            if (cupom.getDataHoraFim().toLocalDate().isBefore(dataReferencia)) {
                iterator.remove();
            }
        }
    }

    @Override
    public Optional<CupomDescontoPedido> buscarCupom(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cuponsDisponiveis.get(codigo));
    }

    private void validarCupom(CupomDescontoPedido cupom) {
        if (cupom == null) {
            throw new IllegalArgumentException("Cupom nao pode ser nulo");
        }

        if (cupom.getCodigo() == null || cupom.getCodigo().isBlank()) {
            throw new IllegalArgumentException("Codigo do cupom nao pode ser vazio");
        }

        if (cupom.getPercentual() <= 0) {
            throw new IllegalArgumentException("Percentual do cupom deve ser maior que zero");
        }

        if (cupom.getDataHoraInicio() == null || cupom.getDataHoraFim() == null) {
            throw new IllegalArgumentException("Periodo de validade do cupom deve ser informado");
        }

        if (cupom.getDataHoraFim().isBefore(cupom.getDataHoraInicio())) {
            throw new IllegalArgumentException("Data final do cupom nao pode ser anterior a data inicial");
        }
    }

    public Map<String, CupomDescontoPedido> getCuponsDisponiveis() {
        return Map.copyOf(cuponsDisponiveis);
    }
}
