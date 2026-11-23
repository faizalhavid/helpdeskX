import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CreateIncidentPayload, Incident, IncidentFilter, IncidentStatus, UpdateIncidentPayload } from "../types/incidents";
import { incidentService } from '../services/incidentService';


export const incidentKeys = {
  all: ['incidents'] as const,
  lists: () => [...incidentKeys.all, 'list'] as const,
  list: (filter: IncidentFilter | undefined) => [...incidentKeys.lists(), { ...filter }] as const,
  details: () => [...incidentKeys.all, 'detail'] as const,
  detail: (id: string) => [...incidentKeys.details(), id] as const,
};



export function useIncidents(filter?: IncidentFilter){
    return useQuery<Incident[]>({
        queryKey: incidentKeys.list(filter),
        queryFn: () => incidentService.getAll(filter),
    });
}

export function useIncident(id: string){
    return useQuery<Incident>({
        queryKey: incidentKeys.detail(id),
        queryFn: () => incidentService.getById(id),
    });
}

export function useCreateIncident(){
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn : (payload:CreateIncidentPayload)  => incidentService.create(payload),
        onSuccess: ()=>{
            queryClient.invalidateQueries({queryKey: incidentKeys.lists()});
        }
    })
}

export function useUpdateIncident(){
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn : ({id, payload}: {id : string, payload : UpdateIncidentPayload}) => incidentService.update(id, payload),
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({queryKey: incidentKeys.lists()});
            queryClient.invalidateQueries({queryKey:incidentKeys.detail(variables.id)});
        }
    })
}

export function useUpdateIncidentStatus(){
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn : ({id, status}: {id : string, status : IncidentStatus}) => incidentService.updateStatus(id, status),
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({queryKey: incidentKeys.lists()});
            queryClient.invalidateQueries({queryKey:incidentKeys.detail(variables.id)});
        }
    })
}

export function useUpdateIncidentAssignedTo(){
    const queryClient = useQueryClient();   
    return useMutation({
        mutationFn : ({id, userId}: {id : string, userId : string}) => incidentService.updateAssignedTo(id, userId),
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({queryKey: incidentKeys.lists()});
            queryClient.invalidateQueries({queryKey:incidentKeys.detail(variables.id)});
        }
    })
}

export function useSoftDeleteIncident(){
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn : (id: string) => incidentService.softDelete(id),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: incidentKeys.lists()});
        }
    })
}


export function useForceDeleteIncident(){
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn : (id: string) => incidentService.forceDelete(id),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: incidentKeys.lists()});
        }
    })
}