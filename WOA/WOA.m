function [Leader_score,Leader_pos,Convergence_curve] = WOA( ...
        SearchAgents_no, Max_iter, lb, ub, dim, fobj)
% Whale Optimization Algorithm (vectorized fitness evaluation)
%
% Inputs:
%   SearchAgents_no : number of whales
%   Max_iter        : max number of iterations
%   lb, ub          : 1×dim vectors of lower/upper bounds
%   dim             : problem dimensionality
%   fobj            : function handle, must accept an N×D matrix and
%                      return an N×1 vector of fitness values
%
% Outputs:
%   Leader_score       : best (minimum) fitness found
%   Leader_pos         : 1×dim position of the best whale
%   Convergence_curve  : 1×Max_iter vector of best fitness at each iter

    % initialize leader
    Leader_pos   = zeros(1,dim);
    Leader_score = inf;              % use -inf for maximization

    % initialize whale positions
    Positions = initialization(SearchAgents_no, dim, ub, lb);

    % preallocate convergence curve
    Convergence_curve = zeros(1, Max_iter);

    t = 0;  % iteration counter
    while t < Max_iter
        %% 1) Boundary control (vectorized)
        Flag4ub   = Positions > ub;    % N×D logical
        Flag4lb   = Positions < lb;
        Positions = Positions .* ~(Flag4ub | Flag4lb) ...
                  + ub        .*  Flag4ub ...
                  + lb        .*  Flag4lb;

        %% 2) Vectorized fitness evaluation
        %    fobj must accept an N×D matrix and return N×1 fitnesses
        fitness = fobj(Positions);    

        %% 3) Leader update (vectorized)
        [best_fitness, best_idx] = min(fitness);  
        if best_fitness < Leader_score
            Leader_score = best_fitness;
            Leader_pos   = Positions(best_idx, :);
        end

        %% 4) Coefficients a and a2
        a  = 2 - t * (2/Max_iter);       % decreases from 2 to 0
        a2 = -1 + t * ((-1)/Max_iter);   % decreases from -1 to -2

        %% 5) Position update
        for i = 1:SearchAgents_no
            r1 = rand();  r2 = rand();
            A  = 2*a*r1 - a;    % (2.3)
            C  = 2*r2;          % (2.4)

            b = 1;                             % spiral constant
            l = (a2 - 1)*rand + 1;            % spiral parameter
            p = rand();                       % probability switch

            if p < 0.5
                if abs(A) >= 1
                    % exploration: move relative to a random whale
                    rand_idx    = randi(SearchAgents_no);
                    X_rand      = Positions(rand_idx, :);
                    D_X_rand    = abs(C*X_rand - Positions(i, :));  % (2.7)
                    Positions(i,:) = X_rand - A*D_X_rand;            % (2.8)
                else
                    % exploitation: move toward the leader
                    D_Leader    = abs(C*Leader_pos - Positions(i, :)); % (2.1)
                    Positions(i,:) = Leader_pos - A*D_Leader;           % (2.2)
                end
            else
                % spiral update (encircling prey)
                distance2Leader    = abs(Leader_pos - Positions(i, :));
                Positions(i,:)     = distance2Leader .* exp(b*l) ...
                                    .* cos(2*pi*l) + Leader_pos;      % (2.5)
            end
        end

        %% 6) Record and display
        t = t + 1;
        Convergence_curve(t) = Leader_score;
        fprintf('Iteration %d — Best score: %g\n', t, Leader_score);
    end
end