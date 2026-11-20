-- V2: situação da temporada (RF01 - virada de temporada)
-- Toda temporada existente nasce ENCERRADA; a 2026 fica VIGENTE.

ALTER TABLE public.temporada
    ADD COLUMN status varchar(20) NOT NULL DEFAULT 'ENCERRADA';

UPDATE public.temporada
   SET status = 'VIGENTE'
 WHERE ano = 2026;

ALTER TABLE public.temporada
    ADD CONSTRAINT ck_temporada_status CHECK (status IN ('VIGENTE', 'ENCERRADA'));

-- Garante que nunca existam duas temporadas vigentes ao mesmo tempo
CREATE UNIQUE INDEX ux_temporada_uma_vigente
    ON public.temporada (status)
    WHERE status = 'VIGENTE';
