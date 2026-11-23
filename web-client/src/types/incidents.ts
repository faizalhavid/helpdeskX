
export enum IncidentStatus {
    NEW = 'NEW',
    ASSIGNED = 'ASSIGNED',
    IN_PROGRESS = 'PROGRESS',
    PENDING = 'PENDING',
    RESOLVED = 'RESOLVED',
    CANCELED = 'CANCELED'
}

export enum IncidentPriority {
    LOW = 'LOW',
    MEDIUM = 'MEDIUM',
    HIGH = 'HIGH',
    CRITICAL = 'CRITICAL'
}

interface IncidentCategory  {
    id : string;
    title :string;
    description?: string | null;
}

export interface Incident extends BaseModel{
    ticket_number : string;
    title : string;
    description: string;
    category : IncidentCategory; 
    status : IncidentStatus;
    priority : IncidentPriority;
    assignedTo? : User | null;
}

export interface IncidentFilter {
    IncidentStatus?: IncidentStatus | 'all';
    IncidentPriority?: IncidentPriority | 'all';
    keyword?: string;
    categoryId?: string;
    startDate?: Date | string;
    endDate?: Date | string;
}

export type CreateIncidentPayload =  Omit<Incident, 'id'>;
export type UpdateIncidentPayload = Partial<Incident>;
